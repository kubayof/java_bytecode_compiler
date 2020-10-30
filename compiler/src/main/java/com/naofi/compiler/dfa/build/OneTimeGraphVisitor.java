package com.naofi.compiler.dfa.build;

import java.util.HashSet;
import java.util.Set;

/**
 * Visit every node exactly once
 */
public class OneTimeGraphVisitor<T> extends BaseGraphVisitor<T> {
    private final Set<Graph.GraphNode> visited = new HashSet<>();

    public final T visit(Graph.GraphNode node) {
        if (visited.contains(node)) {
            return null;
        }

        visited.add(node);
        return super.visit(node);
    }
}
