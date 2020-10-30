package com.naofi.compiler;

import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.dfa.build.Graph;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class GraphBuilderTests {
    @Test
    public void oneBasicBlockGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "int b = a;" +
                        "return a;" +
                        "}",
                0, 1,
                1, 2
        );
    }

    @Test
    public void ifGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "if (a == 0) {" +
                        "a = 1;" +
                        "}" +
                        "return a;" +
                        "}",
                0, 1,
                1, 2,
                3, 4,
                2, 4,
                4, 5,
                2, 3
        );
    }

    @Test
    public void ifElseGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "if (a == 0) {" +
                        "a = 1;" +
                        "} else {" +
                        "a = 2;" +
                        "}" +
                        "return a;" +
                        "}",
                6, 4,
                0, 1,
                1, 2,
                3, 4,
                2, 6,
                4, 5,
                2, 3
        );
    }

    @Test
    public void ifElseIfGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "int c = -1;" +
                        "if (a == 0) {" +
                        "a = 1;" +
                        "} else if (a < 0) {" +
                        "a = 2;" +
                        "}" +
                        "return a;" +
                        "}",
                6, 4,
                6, 4,
                0, 1,
                7, 4,
                1, 2,
                6, 7,
                3, 4,
                2, 6,
                4, 5,
                2, 3
        );
    }

    @Test
    public void ifElseIfElseGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "int c = -1;" +
                        "if (a == 0) {" +
                        "a = 1;" +
                        "} else if (a < 0) {" +
                        "a = 2;" +
                        "} else {" +
                        "a = 3;" +
                        "}" +
                        "return a;" +
                        "}",
                0, 1,
                7, 4,
                1, 2,
                6, 7,
                8, 4,
                3, 4,
                2, 6,
                6, 8,
                4, 5,
                2, 3
        );
    }

    @Test
    public void whileGraph() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "while (a < 10) {" +
                        "a = a + 1;" +
                        "}" +
                        "return a;" +
                        "}",
                0, 1,
                1, 2,
                2, 4,
                3, 2,
                4, 5,
                2, 3
        );
    }

    @Test
    public void emptyBlock() {
        graphBuilderTest(
                "main() {" +
                        "var a = 0;" +
                        "if (a < 10) {" +
                        "} else {" +
                        "a = 1;" +
                        "}" +
                        "return a;" +
                        "}",
                6, 4,
                0, 1,
                1, 2,
                3, 4,
                2, 6,
                4, 5,
                2, 3
        );
    }

    private void graphBuilderTest(String code, int... expectedEdgesNodes) {
        if (expectedEdgesNodes.length % 2 != 0) {
            throw new IllegalStateException("Number of numbers representing edges must be even");
        }
        ParseTree tree = NfCompiler.parse(code, NfLangParser::method);
        Graph graph = Graph.fromMethod((NfLangParser.MethodContext) tree.getChild(0));
//        graph.dumpToFile("DumpedGraph.gv");
        Set<Pair<Integer, Integer>> expectedEdges = new HashSet<>();
        for (int i = 0; i < expectedEdgesNodes.length / 2; i++) {
            expectedEdges.add(new Pair<>(expectedEdgesNodes[2*i], expectedEdgesNodes[2*i+1]));
        }

        Set<Pair<Integer, Integer>> actualEdges = graph.getEdges();

        Assertions.assertEquals(expectedEdges, actualEdges);
    }
}
