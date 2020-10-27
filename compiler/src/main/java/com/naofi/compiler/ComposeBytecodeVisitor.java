package com.naofi.compiler;

import com.naofi.antlr.NfLangBaseVisitor;
import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.symbols.*;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComposeBytecodeVisitor extends NfLangBaseVisitor<VariableType> {
    private final ClassWriter writer;
    private final int bytecodeVersion = Opcodes.V1_8;

    public byte[] getClassBytes() {
        return writer.toByteArray();
    }

    public ComposeBytecodeVisitor() {
        try {
            ClassReader reader = new ClassReader(Objects.requireNonNull(
                    getClass().getClassLoader().getResourceAsStream("java/lang/Object.class")));
            writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public VariableType visitClassDef(NfLangParser.ClassDefContext ctx) {
        if (ctx instanceof TypedClassContext) {
            TypedClassContext clazz = (TypedClassContext) ctx;
            writer.visit(bytecodeVersion, Opcodes.ACC_PUBLIC,
                    clazz.getName(),
                    null,
                    "java/lang/Object",
                    new String[0]);

            MethodVisitor init = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "<init>",
                    "()V",
                    null,
                    null);
            init.visitCode();
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            init.visitInsn(Opcodes.RETURN);
            init.visitMaxs(1, 1);
            init.visitEnd();

            clazz.getMembers().forEach(this::visit);
            writer.visitEnd();
            return null;
        } else {
            throw new IllegalArgumentException(
                    String.format("Class '%s' was not replaced at binding step", ctx.IDENTIFIER().getText()));
        }
    }

    private MethodVisitor methodVisitor;
    private Map<Variable, Integer> methodLocalsMap = new HashMap<>();
    private int localsCounter = 1;

    @Override
    public VariableType visitMethod(NfLangParser.MethodContext ctx) {
        if (ctx instanceof TypedMethodContext) {
            TypedMethodContext typed = (TypedMethodContext) ctx;
            String returnTypeDesc = typed.getReturnTypes().isEmpty() ?
                    VariableType.UNDEFINED.getDescriptor() :
                    typed.getReturnTypes().get(0).getDescriptor();
            methodVisitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    typed.getName(),
                    "()" + returnTypeDesc,
                    null,
                    null);
            methodVisitor.visitCode();
            visit(typed.getBody());
            methodVisitor.visitMaxs(10, methodLocalsMap.size() + 10);
            methodVisitor.visitEnd();

            methodLocalsMap.clear();
            localsCounter = 1;
            return null;
        } else {
            throw new IllegalArgumentException(
                    String.format("Method '%s' was not replaced at binding step", ctx.IDENTIFIER().getText()));
        }
    }

    @Override
    public VariableType visitTypeDef(NfLangParser.TypeDefContext ctx) {
        Variable variable = (Variable)ctx.variable();
        methodLocalsMap.put(variable, localsCounter++);

        return variable.getType();
    }

    @Override
    public VariableType visitTypeInitDef(NfLangParser.TypeInitDefContext ctx) {
        return initVariable((Variable)ctx.variable(), ctx.expr());
    }

    @Override
    public VariableType visitVarInitDef(NfLangParser.VarInitDefContext ctx) {
        return initVariable((Variable)ctx.variable(), ctx.expr());
    }

    private VariableType initVariable(Variable variable, NfLangParser.ExprContext expr) {
        int varIndex = localsCounter++;
        methodLocalsMap.put(variable, varIndex);
        typeToLoad = variable.getType();
        visitExpr(expr);
        assignToLocal(variable.getType(), varIndex);

        return variable.getType();
    }

    @Override
    public VariableType visitAssignment(NfLangParser.AssignmentContext ctx) {
        NfLangParser.VariableContext variableContext = ctx.variable();
        NfLangParser.ExprContext exprContext = ctx.expr();

        if (variableContext instanceof Variable) {
            Variable variable = (Variable) variableContext;
            typeToLoad = variable.getType();
            visitExpr(exprContext);
            int varIndex = methodLocalsMap.get(variable);
            switch (variable.getType()) {
                case BYTE:
                case SHORT:
                case INT:
                    methodVisitor.visitVarInsn(Opcodes.ISTORE, varIndex);
                    break;
                case LONG:
                    methodVisitor.visitVarInsn(Opcodes.LLOAD, varIndex);
                    break;
                default:
                    throw new UnsupportedOperationException("Cannot assign to type: '" + variable.getType() + "'");
            }

            return null;
        }

        throw new IllegalStateException("Variable is assignment is not replaced while binding");
    }

    private void assignToLocal(VariableType type, int varIndex) {
        switch (type) {
            case BOOL:
            case BYTE:
            case SHORT:
            case INT:
                methodVisitor.visitVarInsn(Opcodes.ISTORE, varIndex);
                break;
            case LONG:
                methodVisitor.visitVarInsn(Opcodes.LSTORE, varIndex);
                break;
            default:
                throw new UnsupportedOperationException("Cannot assign to type: " + type);
        }
    }

    private Label endIfLabel = new Label();

    @Override
    public VariableType visitIfStmt(NfLangParser.IfStmtContext ctx) {
        NfLangParser.BoolExpressionContext condition = ctx.boolExpression();
        NfLangParser.BlockContext trueBlock = ctx.block();
        visitBoolExpression(condition);
        if (endIfLabel == null) {
            endIfLabel = new Label();
        }
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, endIfLabel);
        visitBlock(trueBlock);
        methodVisitor.visitLabel(endIfLabel);
        endIfLabel = null;

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitIfElseStmt(NfLangParser.IfElseStmtContext ctx) {
        NfLangParser.BoolExpressionContext condition = ctx.boolExpression();
        NfLangParser.BlockContext trueBlock = ctx.block();
        NfLangParser.ElseInnerContext elseContext = ctx.elseInner();
        visitBoolExpression(condition);
        Label falseLabel = new Label();
        if (endIfLabel == null) {
            endIfLabel = new Label();
        }
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, falseLabel);
        visitBlock(trueBlock);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endIfLabel);
        methodVisitor.visitLabel(falseLabel);
        visit(elseContext);

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitElseStmt(NfLangParser.ElseStmtContext ctx) {
        visit(ctx.block());
        if (endIfLabel != null) {
            methodVisitor.visitLabel(endIfLabel);
            endIfLabel = null;
        }

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitWhileStmt(NfLangParser.WhileStmtContext ctx) {
        Label whileStart = new Label();
        Label whileEnd = new Label();

        methodVisitor.visitLabel(whileStart);
        visitBoolExpression(ctx.boolExpression());
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, whileEnd);
        visitBlock(ctx.block());
        methodVisitor.visitJumpInsn(Opcodes.GOTO, whileStart);
        methodVisitor.visitLabel(whileEnd);

        return VariableType.UNDEFINED;
    }

    @Override
    public VariableType visitReturn(NfLangParser.ReturnContext ctx) {
        if (ctx instanceof TypedReturnContext) {
            TypedReturnContext typed = (TypedReturnContext) ctx;
            typed.getExpressions().forEach(this::visit);
            List<VariableType> returnTypes = typed.getTypes();
            if (returnTypes.size() == 0) {
                methodVisitor.visitInsn(Opcodes.RETURN);
            } else if (returnTypes.size() == 1) {
                switch (returnTypes.get(0)) {
                    case BOOL:
                    case BYTE:
                    case SHORT:
                    case INT:
                        methodVisitor.visitInsn(Opcodes.IRETURN);
                        break;
                    case LONG:
                        methodVisitor.visitInsn(Opcodes.LRETURN);
                        break;
                    case FLOAT:
                        methodVisitor.visitInsn(Opcodes.FRETURN);
                        break;
                    case DOUBLE:
                        methodVisitor.visitInsn(Opcodes.DRETURN);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Unsupported return type '%s'", returnTypes.get(0)));
                }
            } else {
                throw new UnsupportedOperationException(
                        String.format("Returning multiple values is not supported yet: '%s'", ctx.getText()));
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("Return statement '%s' was not replaced at binding step", ctx.getText()));
        }

        return VariableType.UNDEFINED;
    }

    private VariableType typeToLoad;

    @Override
    public VariableType visitEqExpression(NfLangParser.EqExpressionContext ctx) {
        String op = ctx.op4().getText();
        visitExpression(ctx.expression(0));
        visitExpression(ctx.expression(1));

        Label trueLabel = new Label();
        Label endLabel = new Label();
        switch (op) {
            case "==":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            case "!=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + op);
        }
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitLabel(endLabel);

        return VariableType.BOOL;
    }

    @Override
    public VariableType visitCompExpression(NfLangParser.CompExpressionContext ctx) {
        if (ctx.bool_term() != null) {
            visitBool_term(ctx.bool_term());
            return VariableType.BOOL;
        }
        String op = ctx.op3().getText();
        visitExpression(ctx.expression(0));
        visitExpression(ctx.expression(1));

        Label trueLabel = new Label();
        Label endLabel = new Label();
        switch (op) {
            case "<":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "<=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPLE, trueLabel);
                break;
            case ">":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case ">=":
                methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, trueLabel);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operator: " + op);
        }
        methodVisitor.visitInsn(Opcodes.ICONST_0);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);
        methodVisitor.visitLabel(trueLabel);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitLabel(endLabel);

        return VariableType.BOOL;
    }

    @Override
    public VariableType visitExpression(NfLangParser.ExpressionContext ctx) {
        VariableType lastType = visit(ctx.factor(0));
        typeToLoad = ctx.op1().stream()
                .map(op -> ((TypedOp1Context)op).getType())
                .reduce(VariableType::max)
                .orElse(lastType);
        for (int i = 0; i < ctx.op1().size(); i++) {
            visit(ctx.factor(i + 1));
            TypedOp1Context op = (TypedOp1Context) ctx.op1(i);
            if (op.getOperator() == '+') {
                switch (op.getType()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        methodVisitor.visitInsn(Opcodes.IADD);
                        break;
                    case LONG:
                        methodVisitor.visitInsn(Opcodes.LADD);
                        break;
                    case FLOAT:
                        methodVisitor.visitInsn(Opcodes.FADD);
                        break;
                    case DOUBLE:
                        methodVisitor.visitInsn(Opcodes.DADD);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Addition is not supported for type '%s'", op.getType()));
                }
            } else {
                switch (op.getType()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        methodVisitor.visitInsn(Opcodes.ISUB);
                        break;
                    case LONG:
                        methodVisitor.visitInsn(Opcodes.LSUB);
                        break;
                    case FLOAT:
                        methodVisitor.visitInsn(Opcodes.FSUB);
                        break;
                    case DOUBLE:
                        methodVisitor.visitInsn(Opcodes.DSUB);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Addition is not supported for type '%s'", op.getType()));
                }
            }
        }

        return lastType;
    }

    @Override
    public VariableType visitSimpleFactor(NfLangParser.SimpleFactorContext ctx) {
        VariableType resultingType = visitTerm(ctx.term(0));
        for (int i = 0; i < ctx.op2().size(); i++) {
            visitTerm(ctx.term(i + 1));
            TypedOp2Context op = (TypedOp2Context) ctx.op2(i);
            if (op.getOperator() == '*') {
                switch (op.getType()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        methodVisitor.visitInsn(Opcodes.IMUL);
                        break;
                    case LONG:
                        methodVisitor.visitInsn(Opcodes.LMUL);
                        break;
                    case FLOAT:
                        methodVisitor.visitInsn(Opcodes.FMUL);
                        break;
                    case DOUBLE:
                        methodVisitor.visitInsn(Opcodes.DMUL);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Multiplication is not supported for type '%s'", op.getType()));
                }
            } else {
                switch (op.getType()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        methodVisitor.visitInsn(Opcodes.IDIV);
                        break;
                    case LONG:
                        methodVisitor.visitInsn(Opcodes.LDIV);
                        break;
                    case FLOAT:
                        methodVisitor.visitInsn(Opcodes.FDIV);
                        break;
                    case DOUBLE:
                        methodVisitor.visitInsn(Opcodes.DDIV);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                String.format("Division is not supported for type '%s'", op.getType()));
                }
            }
        }

        if (typeToLoad != null) {
            resultingType = typeToLoad;
            typeToLoad = null;
        }
        return resultingType;
    }

    @Override
    public VariableType visitVariable(NfLangParser.VariableContext ctx) {
        if (ctx instanceof Variable) {
            Variable variable = (Variable) ctx;
            int varIndex = methodLocalsMap.get(variable);
            switch (variable.getType()) {
                case BOOL:
                case BYTE:
                case SHORT:
                case INT:
                    methodVisitor.visitIntInsn(Opcodes.ILOAD, varIndex);
                    if (typeToLoad == VariableType.LONG) {
                        methodVisitor.visitInsn(Opcodes.I2L);
                    }
                    break;
                case LONG:
                    methodVisitor.visitIntInsn(Opcodes.LLOAD, varIndex);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Cannot load variable of type '" + variable.getType() + "'");
            }
            return variable.getType();
        }

        throw new IllegalStateException("VariableContext was not replaced at binding step: '" + ctx.getText() + "'");
    }

    @Override
    public VariableType visitBool_term(NfLangParser.Bool_termContext ctx) {
        if (ctx instanceof TypedBoolLiteral) {
            TypedBoolLiteral typed = (TypedBoolLiteral) ctx;
            if (typed.getValue()) {
                methodVisitor.visitInsn(Opcodes.ICONST_1);
            } else {
                methodVisitor.visitInsn(Opcodes.ICONST_0);
            }
            return VariableType.BOOL;
        }
        throw new IllegalStateException("Bool term was not replaced at binding step");
    }

    @Override
    public VariableType visitLiteral(NfLangParser.LiteralContext ctx) {
        if (ctx instanceof TypedLiteralContext) {
            TypedLiteralContext typed = (TypedLiteralContext) ctx;
            long value = typed.getValue();
            VariableType type = (typeToLoad == null) ? typed.getType() : typeToLoad;
            if (type == VariableType.LONG) {
                if (value == 0) {
                    methodVisitor.visitInsn(Opcodes.LCONST_0);
                } else if (value == 1) {
                    methodVisitor.visitInsn(Opcodes.LCONST_1);
                } else {
                    methodVisitor.visitLdcInsn(value);
                }
            } else {
                switch ((int) value) {
                    case 0:
                        methodVisitor.visitInsn(Opcodes.ICONST_0);
                        break;
                    case 1:
                        methodVisitor.visitInsn(Opcodes.ICONST_1);
                        break;
                    case 2:
                        methodVisitor.visitInsn(Opcodes.ICONST_2);
                        break;
                    case 3:
                        methodVisitor.visitInsn(Opcodes.ICONST_3);
                        break;
                    case 4:
                        methodVisitor.visitInsn(Opcodes.ICONST_4);
                        break;
                    case 5:
                        methodVisitor.visitInsn(Opcodes.ICONST_5);
                        break;
                    default:
                        switch (type) {
                            case BYTE:
                                methodVisitor.visitIntInsn(Opcodes.BIPUSH, (int) value);
                                break;
                            case SHORT:
                                methodVisitor.visitIntInsn(Opcodes.SIPUSH, (int) value);
                                break;
                            case INT:
                                methodVisitor.visitLdcInsn((int) value);
                                break;
                            default:
                                throw new UnsupportedOperationException(
                                        String.format("Type '%s' is not supported by bytecode generator", type));
                        }
                }
            }
            return type;
        }



        throw new IllegalStateException("LiteralContext was not replaced at binding step");
    }
}
