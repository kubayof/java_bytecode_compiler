package com.naofi.compiler.binding;

import com.naofi.antlr.NfLangBaseVisitor;
import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.symbols.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Replace terminals and literals with typed, returns type of bound code
 */
public class Binder extends NfLangBaseVisitor<VariableType> {
    private final List<String> errors = new ArrayList<>();
    private final VariableStack stack = new VariableStack();
    private final List<VariableType> methodReturnTypes = new ArrayList<>();

    public Binder() {
        stack.pushScope();
    }

    @Override
    public VariableType visitClassDef(NfLangParser.ClassDefContext ctx) {
        TypedClassContext clazz = new TypedClassContext(ctx);
        replace(ctx, clazz);
        return ctx.classMember().stream()
                .map(this::visit)
                .reduce(this::aggregateResult)
                .orElse(VariableType.UNDEFINED);
    }

    @Override
    public VariableType visitMethod(NfLangParser.MethodContext methodContext) {
        stack.pushScope();
        List<VariableType> formalTypes = methodContext.formalParam().stream()
                .map(this::visitFormalParam).collect(Collectors.toList());
        methodContext.block().statement().forEach(this::visit);

        TypedMethodContext typed = new TypedMethodContext(methodContext, formalTypes, methodReturnTypes);
        replace(methodContext, typed);

        VariableType result = methodReturnTypes.stream()
                .reduce(this::aggregateResult).orElse(VariableType.UNDEFINED);

        stack.popScope();
        methodReturnTypes.clear();

        return result;
    }

    @Override
    public VariableType visitFormalParam(NfLangParser.FormalParamContext ctx) {
        String typeName = ctx.type().getText();
        NfLangParser.VariableContext variableContext = ctx.variable();
        VariableType type = VariableType.of(typeName);
        defineNewVar(variableContext, type);
        stack.findVar(ctx.variable().IDENTIFIER().getText()).setInitialized(true);

        return visitVariable(variableContext);
    }

    @Override
    public VariableType visitIfElseStmt(NfLangParser.IfElseStmtContext ctx) {
        visitBoolExpression(ctx.boolExpression());
        visitBlock(ctx.block());
        visit(ctx.elseInner());

        return VariableType.UNDEFINED;
    }



    @Override
    public VariableType visitIfStmt(NfLangParser.IfStmtContext ctx) {
        visitBoolExpression(ctx.boolExpression());
        visitBlock(ctx.block());

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitWhileStmt(NfLangParser.WhileStmtContext ctx) {
        visitBoolExpression(ctx.boolExpression());
        visitBlock(ctx.block());

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitBlock(NfLangParser.BlockContext ctx) {
        stack.pushScope();
        super.visitBlock(ctx);
        stack.popScope();

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitReturn(NfLangParser.ReturnContext ctx) {
        List<NfLangParser.ExprContext> exprs = ctx.expr();
        List<VariableType> types = exprs.stream()
                .map(this::visit)
                .collect(Collectors.toList());

        if (!methodReturnTypes.isEmpty()) {
            if (!methodReturnTypes.equals(types)) {
                errors.add("All types in all method return statements must match");
            }
        } else {
            methodReturnTypes.addAll(types);
        }

        TypedReturnContext typed = new TypedReturnContext(ctx, types);
        replace(ctx, typed);

        return types.stream().reduce(this::aggregateResult).orElse(VariableType.UNDEFINED);
    }

    @Override
    public VariableType visitTypeInitDef(NfLangParser.TypeInitDefContext ctx) {
        NfLangParser.TypeContext typeContext = ctx.type();
        NfLangParser.VariableContext variableContext = ctx.variable();
        NfLangParser.ExprContext exprContext = ctx.expr();
        VariableType variableType = VariableType.of(typeContext.getText());
        VariableType expressionType = visit(exprContext);
        if (!variableType.isAssignableFrom(expressionType)) {
            errors.add(String.format("Cannot assign type %s to variable of type %s", expressionType, variableType));
        }
        defineNewVar(variableContext, variableType);
        stack.findVar(ctx.variable().IDENTIFIER().getText()).setInitialized(true);

        return visitVariable(variableContext);
    }

    @Override
    public VariableType visitTypeDef(NfLangParser.TypeDefContext ctx) {
        NfLangParser.TypeContext typeContext = ctx.type();
        NfLangParser.VariableContext variableContext = ctx.variable();
        VariableType type = VariableType.of(typeContext.getText());
        defineNewVar(variableContext, type);

        Variable var = stack.findVar(ctx.variable().IDENTIFIER().getText());
        replace(ctx, var);
        return type;
    }

    @Override
    public VariableType visitVarInitDef(NfLangParser.VarInitDefContext ctx) {
        NfLangParser.VariableContext variable = ctx.variable();
        NfLangParser.ExprContext expr = ctx.expr();
        VariableType type = visit(expr);
        defineNewVar(variable, type);
        stack.findVar(ctx.variable().IDENTIFIER().getText()).setInitialized(true);

        return visitVariable(variable);
    }

    @Override
    public VariableType visitEqExpression(NfLangParser.EqExpressionContext ctx) {
        super.visitEqExpression(ctx);

        return VariableType.BOOL;
    }

    @Override
    public VariableType visitCompExpression(NfLangParser.CompExpressionContext ctx) {
        if (ctx.bool_term() != null) {
            boolean value = Boolean.parseBoolean(ctx.getText());
            TypedBoolLiteral typed = new TypedBoolLiteral(ctx.bool_term(), VariableType.BOOL, value);
            replace(ctx.bool_term(), typed);
            return VariableType.BOOL;
        }
        VariableType type1 = visitExpression(ctx.expression(0));
        VariableType type2 = visitExpression(ctx.expression(1));
        if (!type1.isAssignableFrom(type2)) {
            errors.add(String.format("Cannot compare types '%s' and '%s'", type1, type2));
        }

        return VariableType.BOOL;
    }

    @Override
    public VariableType visitExpression(NfLangParser.ExpressionContext ctx) {
        VariableType type = visit(ctx.factor(0));
        for (int i = 0; i < ctx.op1().size(); i++) {
            type = VariableType.max(type, visit(ctx.factor(i + 1)));
            NfLangParser.Op1Context op = ctx.op1(i);
            TypedOp1Context typed = new TypedOp1Context(op, type);
            replace(op, typed);
        }

        return type;
    }

    @Override
    public VariableType visitSimpleFactor(NfLangParser.SimpleFactorContext ctx) {
        VariableType type = visitTerm(ctx.term(0));
        for (int i = 0; i < ctx.op2().size(); i++) {
            type = VariableType.max(type, visitTerm(ctx.term(i + 1)));
            NfLangParser.Op2Context op = ctx.op2(i);
            TypedOp2Context typed = new TypedOp2Context(op, type);
            replace(op, typed);
        }

        return type;
    }

    @Override
    public VariableType visitVariable(NfLangParser.VariableContext ctx) {
        Variable var = stack.findVar(ctx);
        if (var == null) {
            errors.add(String.format("Variable '%s' is not defined in scope", ctx.IDENTIFIER().getText()));
            return VariableType.UNDEFINED;
        }

        if (!var.isInitialized()) {
            errors.add(String.format("Variable '%s' may be not initialized", ctx.IDENTIFIER().getText()));
        }
        replace(ctx, var);
        return var.getType();
    }

    @Override
    public VariableType visitLiteral(NfLangParser.LiteralContext ctx) {
        TypedLiteralContext typed = new TypedLiteralContext(ctx);
        replace(ctx, typed);

        return typed.getType();
    }

    public List<String> getErrors() {
        return errors;
    }

    private void replace(ParserRuleContext oldContext, ParserRuleContext newContext) {
        ParserRuleContext parent = oldContext.getParent();
        List<ParseTree> children = parent.children;
        int i;
        for (i = 0; i < children.size(); i++) {
            if ((children.get(i) == oldContext) ||
                    children.get(i).getText().equals(oldContext.getText())) {
                break;
            }
        }
        children.set(i, newContext);
    }

    private void defineNewVar(NfLangParser.VariableContext variable, VariableType type) {
        Variable newVar = stack.defineNewVar(variable, type);
        if (newVar == null) {
            errors.add(String.format("Variable '%s' is already defined in scope", variable.IDENTIFIER().getText()));
        }
    }

    @Override
    protected VariableType defaultResult() {
        return VariableType.ANY;
    }

    @Override
    protected VariableType aggregateResult(VariableType aggregate, VariableType nextResult) {
        if (aggregate == VariableType.ANY) {
            return nextResult;
        }
        if (nextResult == VariableType.ANY) {
            return aggregate;
        }
        if (aggregate.isAssignableFrom(nextResult) || nextResult.isAssignableFrom(aggregate)) {
            return (aggregate.ordinal() > nextResult.ordinal()) ? aggregate : nextResult;
        }
        errors.add(String.format("Cannot find common type for '%s' and '%s'", aggregate, nextResult));
        return VariableType.UNDEFINED;
    }
}
