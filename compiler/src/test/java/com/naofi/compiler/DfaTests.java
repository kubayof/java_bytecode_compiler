package com.naofi.compiler;

import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.Binder;
import com.naofi.compiler.dfa.DfaPipeline;
import com.naofi.compiler.dfa.build.Graph;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class DfaTests {
    @Test
    public void returnInitialized() {
        varInitTest("main() {" +
                "int a = 0;" +
                "return a;" +
                "}");
    }

    @Test
    public void returnUninitialized() {
        varInitTest("main() {" +
                        "int a;" +
                        "return a;" +
                        "}",
                "Variable 'a' may be not initialized"
        );
    }

    @Test
    public void initializeInIf() {
        varInitTest("main() {" +
                        "int a;" +
                        "if (false) {" +
                        "a = 9;" +
                        "}" +
                        "return a;" +
                        "}",
                "Variable 'a' may be not initialized"
        );
    }

    @Test
    public void initializeInIfAndElse() {
        varInitTest("main() {" +
                "int a;" +
                "if (false) {" +
                "a = 9;" +
                "} else {" +
                "a = 2;" +
                "}" +
                "return a;" +
                "}"
        );
    }

    @Test
    public void initializeInIfAndElse2() {
        varInitTest("main() {" +
                "int a;" +
                "int c;" +
                "if (false) {" +
                "c = 3;" +
                "} else {" +
                "a = 2;" +
                "}" +
                "return a;" +
                "}",
                "Variable 'a' may be not initialized"
        );
    }

    @Test
    public void initializeInWhile() {
        varInitTest("main() {" +
                        "int a;" +
                        "while (1 > 2) {" +
                        "a = 1;" +
                        "}" +
                        "return a;" +
                        "}",
                "Variable 'a' may be not initialized"
        );
    }

    private void varInitTest(String code, String... expectedErrors) {
        Binder binder = new Binder();
        ParseTree tree = NfCompiler.parse(code, NfLangParser::method);
        binder.visit(tree);
        Graph graph = Graph.fromMethod((NfLangParser.MethodContext) tree.getChild(0));
//        graph.dumpToFile("DumpedGraph.gv");
        List<String> actualErrors = DfaPipeline.run(graph);

        Assertions.assertEquals(Arrays.asList(expectedErrors), actualErrors);
    }
}
