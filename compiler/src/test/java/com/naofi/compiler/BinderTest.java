package com.naofi.compiler;

import com.naofi.antlr.NfLangParser;
import com.naofi.compiler.binding.Binder;
import com.naofi.compiler.binding.symbols.VariableType;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class BinderTest {
    @Test
    public void simpleExpression() {
        testMethodBinding(
                "main() {" +
                        "return 1 + 2;" +
                        "}",
                VariableType.BYTE
        );
    }

    @Test
    public void typeDef1() {
        testMethodBinding(
                "main () {" +
                        "byte a = 0;" +
                        "return a;" +
                        "}",
                VariableType.BYTE
        );
    }

    @Test
    public void typeDef2() {
        testMethodBinding(
                "main () {" +
                        "long a = 0;" +
                        "return a;" +
                        "}",
                VariableType.LONG
        );
    }

    @Test
    public void varDef1() {
        testMethodBinding(
                "main () {" +
                        "var a = 0;" +
                        "return a;" +
                        "}",
                VariableType.BYTE
        );
    }

    @Test
    public void varDef2() {
        testMethodBinding(
                "main () {" +
                        "long a = 0;" +
                        "float b = 1;" +
                        "var c = a + b;" +
                        "return c;" +
                        "}",
                VariableType.FLOAT
        );
    }

    @Test
    public void varDef3() {
        testMethodBinding(
                "main () {" +
                        "var a = 128;" +
                        "return a;" +
                        "}",
                VariableType.SHORT
        );
    }

    @Test
    public void varDef4() {
        testMethodBinding(
                "main () {" +
                        "var a = 32768;" +
                        "return a;" +
                        "}",
                VariableType.INT
        );
    }

    @Test
    public void varDef5() {
        System.err.println(Integer.MAX_VALUE);
        testMethodBinding(
                "main () {" +
                        "var a = 32768;" +
                        "return a;" +
                        "}",
                VariableType.INT
        );
    }

    @Test
    public void expressionType1() {
        testMethodBinding(
                "main () {" +
                        "long a = 1;" +
                        "int b = 2;" +
                        "return a + b;" +
                        "}",
                VariableType.LONG
        );
    }

    @Test
    public void redefineVariable1() {
        testMethodBinding(
                "main () {" +
                        "long a;" +
                        "int a;" +
                        "}",
                VariableType.UNDEFINED,
                "Variable 'a' is already defined in scope"
        );
    }

    @Test
    public void redefineVariable2() {
        testMethodBinding(
                "main (double a) {" +
                        "float a;" +
                        "}",
                VariableType.UNDEFINED,
                "Variable 'a' is already defined in scope"
        );
    }

    @Test
    @Disabled
    public void uninitializedVariable() {
        testMethodBinding(
                "main () {" +
                        "float a;" +
                        "double b = a;" +
                        "}",
                VariableType.UNDEFINED,
                "Variable 'a' may be not initialized"
        );
    }

    @Test
    public void assignBoolToInt() {
        testMethodBinding(
                "main () {" +
                        "bool a = false;" +
                        "int b = a;" +
                        "int c = false;" +
                        "}",
                VariableType.UNDEFINED,
                "Cannot assign type BOOL to variable of type INT",
                "Cannot assign type BOOL to variable of type INT"
        );
    }

    @Test
    public void assignIntToBool() {
        testMethodBinding(
                "main () {" +
                        "int a = 0;" +
                        "bool b = a;" +
                        "bool c = 1;" +
                        "}",
                VariableType.UNDEFINED,
                "Cannot assign type INT to variable of type BOOL",
                "Cannot assign type BYTE to variable of type BOOL"
        );
    }

    @Test
    public void returnEqByte() {
        testMethodBinding(
                "main () {" +
                        "var a = 0;" +
                        "var b = a;" +
                        "return a == b;" +
                        "}",
                VariableType.BOOL
        );
    }

    @Test
    public void returnNotEqByte() {
        testMethodBinding(
                "main () {" +
                        "var a = 0;" +
                        "var b = a;" +
                        "return a != b;" +
                        "}",
                VariableType.BOOL
        );
    }

    @Test
    public void returnCompByte() {
        testMethodBinding(
                "main () {" +
                        "var a = 0;" +
                        "var b = a;" +
                        "return a >= b;" +
                        "}",
                VariableType.BOOL
        );
    }

    @Test
    public void ifScope() {
        testMethodBinding(
                "main () {" +
                        "if (true) {" +
                        "var a = false;" +
                        "}" +
                        "return a;" +
                        "}",
                VariableType.UNDEFINED,
                "Variable 'a' is not defined in scope"
        );
    }

    private void testMethodBinding(String code, VariableType expectedType, String... expectedErrors) {
        Binder binder = new Binder();
        ParseTree tree = NfCompiler.parse(code, NfLangParser::method);
        VariableType actualType = binder.visit(tree);
        Assertions.assertEquals(expectedType, actualType);

        List<String> actualErrors = binder.getErrors();
        Assertions.assertEquals(Arrays.asList(expectedErrors), actualErrors);
    }
}
