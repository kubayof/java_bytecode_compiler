package com.naofi.compiler.dfa;

import com.naofi.antlr.NfLangBaseVisitor;
import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.symbols.Variable;
import com.naofi.compiler.dfa.build.BaseGraphVisitor;
import com.naofi.compiler.dfa.build.Graph;
import com.naofi.compiler.dfa.build.IgnoreBackLinksGraphVisitor;

import java.util.*;

/**
 * Check if variable was initialized before usage
 */
class VarInitChecker extends IgnoreBackLinksGraphVisitor<String> implements DfaPipelineEntry {
    private final List<String> errors = new ArrayList<>();
    // Contains variables, deque because of conditional jump
    private final Deque<Map<Variable, Boolean>> varInits = new ArrayDeque<>();

    @Override
    public List<String> check(Graph.StartNode startNode) {
        visitStartNode(startNode);
        return errors;
    }

    private class StatementVisitor extends NfLangBaseVisitor<String> {
        @Override
        public String visitAssignment(NfLangParser.AssignmentContext ctx) {
            visitExpr(ctx.expr());
            Variable variable = (Variable) ctx.variable();
            initVar(variable);

            return null;
        }

        @Override
        public String visitTypeDef(NfLangParser.TypeDefContext ctx) {
            if (ctx.variable() instanceof Variable) {
                Variable variable = (Variable) ctx.variable();
                defineVar(variable);
                return null;
            }

            return varNotReplaced();
        }

        @Override
        public String visitTypeInitDef(NfLangParser.TypeInitDefContext ctx) {
            if (ctx.variable() instanceof Variable) {
                visitExpr(ctx.expr());
                Variable variable = (Variable) ctx.variable();
                defineVar(variable);
                initVar(variable);
                return null;
            }

            return varNotReplaced();
        }

        @Override
        public String visitVarInitDef(NfLangParser.VarInitDefContext ctx) {
            if (ctx.variable() instanceof Variable) {
                visitExpr(ctx.expr());
                Variable variable = (Variable) ctx.variable();
                defineVar(variable);
                initVar(variable);
                return null;
            }

            return varNotReplaced();
        }

        @Override
        public String visitVariable(NfLangParser.VariableContext ctx) {
            if (ctx instanceof Variable) {
                checkUsage((Variable) ctx);
                return null;
            }

            return varNotReplaced();
        }

        private String varNotReplaced() {
            throw new IllegalStateException("Variable was not replaced on binding step");
        }
    }

    @Override
    public String visit(Graph.GraphNode node) {
        pushScope();
        String result = super.visit(node);
        popScope();

        return  result;
    }

    private final StatementVisitor statementVisitor  = new StatementVisitor();
    @Override
    public String visitBasicBlock(Graph.BasicBlock node) {
        node.getStatements().forEach(statementVisitor::visit);
        return visit(node.getNext());
    }

    @Override
    public String visitConditionalJump(Graph.ConditionalJump node) {
        statementVisitor.visit(node.getCondition());
        pushScope();
        visit(node.getIfTrue());
        popScope();
        pushScope();
        visit(node.getIfFalse());
        popScope();
        return null;
    }

    public List<String> getErrors() {
        return errors;
    }

    private void checkUsage(Variable variable) {
        if (!isInitialized(variable)) {
            errors.add(String.format("Variable '%s' may be not initialized", variable.getName()));
        }
    }

    private boolean isInitialized(Variable variable) {
        return varInits.stream()
                .map(scope -> scope.getOrDefault(variable, false))
                .filter(v -> v)
                .findAny()
                .orElse(false);
    }

    private void defineVar(Variable variable) {
        varInits.getFirst().put(variable, false);
    }

    private void initVar(Variable variable) {
        varInits.getFirst().computeIfPresent(variable, (k, v) -> true);
//        varInits.forEach(map -> map.computeIfPresent(variable, (k, v) -> true));
    }

    private void pushScope() {
        if (varInits.isEmpty()) {
            varInits.push(new IdentityHashMap<>());
        } else {
            varInits.push(new IdentityHashMap<>(varInits.getFirst()));
        }
    }

    private void popScope() {
        varInits.pop();
    }
}
