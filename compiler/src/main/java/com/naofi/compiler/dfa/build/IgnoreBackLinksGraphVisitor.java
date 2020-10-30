package com.naofi.compiler.dfa.build;


import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Ignores node if it is on visited nodes stack
 * IMPORTANT!!!
 *      use only method IgnoreBackLinksGraphVisitor::visit for correct work
 */
public class IgnoreBackLinksGraphVisitor<T> extends BaseGraphVisitor<T> {
    private final Deque<Graph.GraphNode> visitedNodesStack = new ArrayDeque<>();

    @Override
    public T visit(Graph.GraphNode node) {
        if (visitedNodesStack.contains(node)) {
            return defaultReturnValue();
        }
        visitedNodesStack.push(node);
        T result = super.visit(node);
        visitedNodesStack.pop();

        return result;
    }
}
