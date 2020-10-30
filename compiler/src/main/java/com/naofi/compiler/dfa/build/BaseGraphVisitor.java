package com.naofi.compiler.dfa.build;

public class BaseGraphVisitor<T> {
    public T visit(Graph.GraphNode node) {
        if (node instanceof Graph.StartNode) {
            return visitStartNode((Graph.StartNode) node);
        } else if (node instanceof Graph.EndNode) {
            return visitEndNode((Graph.EndNode) node);
        } else if (node instanceof Graph.BasicBlock) {
            return visitBasicBlock((Graph.BasicBlock) node);
        } else if (node instanceof Graph.ConditionalJump) {
            return visitConditionalJump((Graph.ConditionalJump) node);
        } else {
            throw new UnsupportedOperationException("Unknown type of graph node: " + node.getClass().getName());
        }
    }

    protected T visitStartNode(Graph.StartNode node) {
        return visit(node.getNext());
    }

    protected T visitEndNode(Graph.EndNode node) {
        return null;
    }

    protected T visitBasicBlock(Graph.BasicBlock node) {
        return visit(node.getNext());
    }

    protected T visitConditionalJump(Graph.ConditionalJump node) {
        visit(node.getIfTrue());
        visit(node.getIfFalse());
        return defaultReturnValue();
    }

    protected T defaultReturnValue() {
        return null;
    }
}
