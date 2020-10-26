package com.naofi.compiler.dfa;

import com.naofi.antlr.NfLangParser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Graph {
    public static Graph from(ParseTree method) {
        return null;
    }

    public static class BasicBlock {
        private final List<NfLangParser.StatementContext> statements = new ArrayList<>();

        public BasicBlock(List<NfLangParser.StatementContext> statements) {
            this.statements.addAll(statements);
        }

        public BasicBlock(NfLangParser.StatementContext... statements) {
            this.statements.addAll(Arrays.asList(statements));
        }

        public final List<RuleContext> getStatements() {
            return new ArrayList<>(statements);
        }
    }

    public static class ConditionalJump {
        private final NfLangParser.ExpressionContext condition;
        private final BasicBlock ifTrue;
        private final BasicBlock ifFalse;

        public ConditionalJump(NfLangParser.ExpressionContext condition, BasicBlock ifTrue, BasicBlock ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        public final NfLangParser.ExpressionContext getCondition() {
            return condition;
        }

        public final BasicBlock getIfTrue() {
            return ifTrue;
        }

        public final BasicBlock getIfFalse() {
            return ifFalse;
        }
    }

    public static class Jump {
        private final BasicBlock to;

        public Jump(BasicBlock to) {
            this.to = to;
        }

        public final BasicBlock getDestination() {
            return to;
        }
    }
}
