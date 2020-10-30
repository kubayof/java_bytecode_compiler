package com.naofi.compiler.dfa;

import com.naofi.compiler.dfa.build.BaseGraphVisitor;
import com.naofi.compiler.dfa.build.Graph;
import com.naofi.compiler.dfa.build.IgnoreBackLinksGraphVisitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Removes empty basic blocks from cfg
 */
class EliminateEmptyBlocksVisitor extends IgnoreBackLinksGraphVisitor<String> implements DfaPipelineEntry {
    private final Deque<Consumer<Graph.GraphNode>> prevSetters = new ArrayDeque<>();

    @Override
    public List<String> check(Graph.StartNode startNode) {
        visitStartNode(startNode);
        return null;
    }

    @Override
    public String visitStartNode(Graph.StartNode node) {
        pushSetter(node::setNext);
        visit(node.getNext());
        popSetter();
        return null;
    }

    @Override
    public String visitEndNode(Graph.EndNode node) {
        return super.visitEndNode(node);
    }

    @Override
    public String visitBasicBlock(Graph.BasicBlock node) {
        if (node.getStatements().isEmpty()) {
            setPreviousNext(node.getNext());
            return visit(node.getNext());
        }
        pushSetter(node::setNext);
        visit(node.getNext());
        popSetter();
        return null;
    }

    @Override
    public String visitConditionalJump(Graph.ConditionalJump node) {
        pushSetter(node::setIfTrue);
        visit(node.getIfTrue());
        popSetter();
        pushSetter(node::setIfFalse);
        visit(node.getIfFalse());
        popSetter();

        return null;
    }

    private void pushSetter(Consumer<Graph.GraphNode> setter) {
        prevSetters.push(setter);
    }

    private void popSetter() {
        prevSetters.pop();
    }

    private void setPreviousNext(Graph.GraphNode node) {
        prevSetters.getFirst().accept(node);
    }
}
