package com.naofi.compiler.dfa;

import com.naofi.antlr.NfLangBaseVisitor;
import com.naofi.antlr.NfLangParser;

import static com.naofi.compiler.dfa.GraphBuilder.Label;

public class BuildGraphVisitor  extends NfLangBaseVisitor<Graph.GraphNode> {
    private final GraphBuilder builder = new GraphBuilder();

    @Override
    public Graph.GraphNode visitWhileStmt(NfLangParser.WhileStmtContext ctx) {
        Label condLabel = new Label();
        Label startLabel = new Label();
        Label endLabel = new Label();

        builder.label(condLabel);
        builder.condJump(ctx.boolExpression(), startLabel, endLabel);
        builder.label(startLabel);
        visitBlock(ctx.block());
        builder.goTo(condLabel);
        builder.label(endLabel);

        return null;
    }

    @Override
    public Graph.GraphNode visitIfStmt(NfLangParser.IfStmtContext ctx) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();


        builder.condJump(ctx.boolExpression(), trueLabel, falseLabel);
        builder.label(trueLabel);
        visitBlock(ctx.block());
        builder.label(falseLabel);
        if (endLabel != null) {
            builder.label(endLabel);
            endLabel = null;
        }

        return null;
    }

    private Label endLabel;
    @Override
    public Graph.GraphNode visitIfElseStmt(NfLangParser.IfElseStmtContext ctx) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        if (endLabel == null) {
            endLabel = new Label();
        }

        builder.condJump(ctx.boolExpression(), trueLabel, falseLabel);
        builder.label(trueLabel);
        visitBlock(ctx.block());
        builder.goTo(endLabel);
        builder.label(falseLabel);
        visit(ctx.elseInner());

        return null;
    }

    @Override
    public Graph.GraphNode visitElseStmt(NfLangParser.ElseStmtContext ctx) {
        visitBlock(ctx.block());
        if (endLabel != null) {
            builder.label(endLabel);
            endLabel = null;
        } else {
            throw new IllegalStateException("endLabel is null while visiting elseStmt");
        }

        return null;
    }

    @Override
    public Graph.GraphNode visitSimpleStatement(NfLangParser.SimpleStatementContext ctx) {
        builder.basic(ctx);
        return null;
    }

    @Override
    public Graph.GraphNode visitAssignment(NfLangParser.AssignmentContext ctx) {
        builder.basic(ctx);
        return null;
    }

    @Override
    public Graph.GraphNode visitTypeDef(NfLangParser.TypeDefContext ctx) {
        builder.basic(ctx);
        return null;
    }

    @Override
    public Graph.GraphNode visitTypeInitDef(NfLangParser.TypeInitDefContext ctx) {
        builder.basic(ctx);
        return null;
    }

    @Override
    public Graph.GraphNode visitVarInitDef(NfLangParser.VarInitDefContext ctx) {
        builder.basic(ctx);
        return null;
    }

    @Override
    public Graph.GraphNode visitReturn(NfLangParser.ReturnContext ctx) {
        builder.basic(ctx);
        builder.end();
        return null;
    }

    public Graph.StartNode getStartNode() {
        return builder.getStartNode();
    }
}
