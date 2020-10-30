package com.naofi.compiler.dfa;

import com.naofi.compiler.dfa.build.Graph;

import java.util.List;

public interface DfaPipelineEntry {
    /**
     * Returns list of errors
     */
    List<String> check(Graph.StartNode startNode);
}
