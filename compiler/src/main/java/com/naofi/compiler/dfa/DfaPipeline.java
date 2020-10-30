package com.naofi.compiler.dfa;

import com.naofi.compiler.dfa.build.Graph;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DfaPipeline {
    public static List<String> run(Graph graph) {
        return new DfaPipeline().run(graph.getStartNode());
    }

    private final DfaPipelineEntry[] entries = {
            new EliminateEmptyBlocksVisitor(),
            new VarInitChecker()
    };

    private List<String> run(Graph.StartNode startNode) {
        return Arrays.stream(entries)
                .map(entry -> entry.check(startNode))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private DfaPipeline() {}
}
