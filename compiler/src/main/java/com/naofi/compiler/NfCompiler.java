package com.naofi.compiler;

import com.naofi.antlr.NfLangLexer;
import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.Binder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

public class NfCompiler {
    public static byte[] compile(String code) {
        ParseTree tree = parse(code, NfLangParser::classDef);
        Binder binder = new Binder();
        binder.visit(tree);
        if (binder.getErrors().size() != 0) {
            binder.getErrors().forEach(System.err::println);
            return null;
        }
        ComposeBytecodeVisitor cbv = new ComposeBytecodeVisitor();
        cbv.visit(tree);
        return cbv.getClassBytes();
    }

    public static ParseTree parse(String code, Function<NfLangParser, ParseTree> parseFunction) {
        CharStream chars = CharStreams.fromString(code);
        NfLangLexer lexer = new NfLangLexer(chars);
        TokenStream tokens = new CommonTokenStream(lexer);
        NfLangParser parser = new NfLangParser(tokens);
        ParseTree tree = parseFunction.apply(parser);
        // To avoid NullPointerException while replacing
        ParserRuleContext rootContext = new ParserRuleContext();
        rootContext.children = new ArrayList<>();
        rootContext.children.add(tree);
        tree.setParent(rootContext);

        return rootContext;
    }

    private static byte[] dummy() {
        try {
            return Files.readAllBytes(Paths.get(Objects.requireNonNull(
                    NfCompiler.class.getClassLoader()
                            .getResource("com/naofi/compiler/test/Main.class"))
                    .getPath()));
        } catch (IOException e) {
            return null;
        }
    }
}
