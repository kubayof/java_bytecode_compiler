package com.naofi.compiler.dfa;

import com.naofi.antlr.NfLangBaseVisitor;
import com.naofi.antlr.NfLangParser;
import org.antlr.v4.runtime.misc.Pair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * StartNode
 * EndNode
 * BasicBlock
 * ConditionalJump
 */
public class Graph {
    public static Graph fromMethod(NfLangParser.MethodContext method) {
        BuildGraphVisitor builder = new BuildGraphVisitor();
        builder.visitMethod(method);
        return new Graph(builder.getStartNode());
    }

    public interface GraphNode {
    }

    public static class StartNode implements GraphNode {
        private GraphNode next;

        public void setNext(GraphNode next) {
            this.next = next;
        }

        public GraphNode getNext() {
            return next;
        }

        @Override
        public String toString() {
            return "Start";
        }
    }

    public static class EndNode implements GraphNode {
        @Override
        public String toString() {
            return "End";
        }
    }

    public static class BasicBlock implements GraphNode {
        private final List<NfLangParser.StatementContext> statements = new ArrayList<>();
        private GraphNode next;

        public BasicBlock(List<NfLangParser.StatementContext> statements) {
            this.statements.addAll(statements);
        }

        public BasicBlock(NfLangParser.StatementContext... statements) {
            this.statements.addAll(Arrays.asList(statements));
        }

        public final List<NfLangParser.StatementContext> getStatements() {
            return new ArrayList<>(statements);
        }

        public void addStatement(NfLangParser.StatementContext stmt) {
            statements.add(stmt);
        }

        public GraphNode getNext() {
            return next;
        }

        public void setNext(GraphNode next) {
            this.next = next;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Basic{\n");
            statements.forEach(s -> builder.append(s.getText()).append('\n'));
            builder.append("}");
            return builder.toString();
        }
    }

    public static class ConditionalJump implements GraphNode {
        private final NfLangParser.BoolExpressionContext condition;
        private GraphNode ifTrue;
        private GraphNode ifFalse;

        public ConditionalJump(NfLangParser.BoolExpressionContext condition) {
            this.condition = condition;
        }

        public NfLangParser.BoolExpressionContext getCondition() {
            return condition;
        }

        public GraphNode getIfTrue() {
            return ifTrue;
        }

        public GraphNode getIfFalse() {
            return ifFalse;
        }

        public void setIfTrue(GraphNode ifTrue) {
            this.ifTrue = ifTrue;
        }

        public void setIfFalse(GraphNode ifFalse) {
            this.ifFalse = ifFalse;
        }

        @Override
        public String toString() {
            return "Cond{" + condition.getText() + '}';
        }
    }

    private static class DumpGraphVisitor extends BaseGraphVisitor<Integer> {
        private final StringBuilder dotGraphBuilder = new StringBuilder();
        private final Set<Pair<Integer, Integer>> edges = new HashSet<>(); //for testing
        //For nodes used more than once
        private final Map<GraphNode, Integer> traversedNodes = new IdentityHashMap<>();
        private int lastNodeNumber = 0;

        @Override
        public Integer visitStartNode(StartNode node) {
            int nodeNumber = nodeNumber(node);
            dotGraphBuilder.append("digraph DfaGraph {")
                    .append(nodeNumber)
                    .append(" [label=Start, color=green];");
            int nextNodeNumber = visit(node.getNext());
            dotGraphBuilder.append(nodeNumber).append(" -> ").append(nextNodeNumber).append(";");
            edges.add(new Pair<>(nodeNumber, nextNodeNumber));

            return nodeNumber;
        }

        @Override
        public Integer visitEndNode(EndNode node) {
            if (traversedNodes.containsKey(node)) {
                return traversedNodes.get(node);
            }
            int nodeNumber = nodeNumber(node);
            dotGraphBuilder.append(nodeNumber)
                    .append(" [label=End, color=red];");
            return nodeNumber;
        }

        @Override
        public Integer visitBasicBlock(BasicBlock node) {
            if (traversedNodes.containsKey(node)) {
                return traversedNodes.get(node);
            }
            int nodeNumber = nodeNumber(node);
            dotGraphBuilder.append(nodeNumber)
                    .append(" [shape=box, label=\"");
            for (NfLangParser.StatementContext statement : node.getStatements()) {
                dotGraphBuilder.append(statement.getText()).append("\n");
            }
            dotGraphBuilder.append("\"];");
            int nextNodeNumber = visit(node.getNext());
            dotGraphBuilder.append(nodeNumber).append(" -> ").append(nextNodeNumber).append(";");
            edges.add(new Pair<>(nodeNumber, nextNodeNumber));

            return nodeNumber;
        }

        @Override
        public Integer visitConditionalJump(ConditionalJump node) {
            if (traversedNodes.containsKey(node)) {
                return traversedNodes.get(node);
            }
            int nodeNumber = nodeNumber(node);
            dotGraphBuilder.append(nodeNumber)
                    .append("[label=\"")
                    .append(node.condition.getText())
                    .append("\", shape=diamond];");
            int trueNodeNumber = visit(node.getIfTrue());
            dotGraphBuilder.append(nodeNumber).append(" -> ").append(trueNodeNumber).append("[label=\"+\"];");
            edges.add(new Pair<>(nodeNumber, trueNodeNumber));
            int falseNodeNumber = visit(node.getIfFalse());
            dotGraphBuilder.append(nodeNumber).append(" -> ").append(falseNodeNumber).append("[label=\"-\"];");
            edges.add(new Pair<>(nodeNumber, falseNodeNumber));

            return nodeNumber;
        }

        private int nodeNumber(GraphNode node) {
            // getOrDefault can be used, but lastNodeNumber will hbe incremented in any case
            if (traversedNodes.containsKey(node)) {
                return traversedNodes.get(node);
            }
            int newNumber = lastNodeNumber++;
            traversedNodes.put(node, newNumber);

            return newNumber;
        }

        public String getText() {
            return dotGraphBuilder.append("}").toString();
        }

        public Set<Pair<Integer, Integer>> getEdges() {
            return edges;
        }
    }


    private final GraphNode rootNode;

    private Graph(GraphNode rootNode) {
        this.rootNode = rootNode;
    }

    public void dumpToFile(String fileName) {
        try (PrintStream printer = new PrintStream(new FileOutputStream(fileName))) {
            printer.println(dumpToString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String dumpToString() {
        DumpGraphVisitor dumper = new DumpGraphVisitor();
        dumper.visit(rootNode);
        return dumper.getText();
    }

    public Set<Pair<Integer, Integer>> getEdges() {
        DumpGraphVisitor dumper = new DumpGraphVisitor();
        dumper.visit(rootNode);
        return dumper.getEdges();
    }

}
