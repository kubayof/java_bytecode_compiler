package com.naofi.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CompilerTest {
    @Test
    public void returnConstantLessThan6() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1;" +
                        "}" +
                        "}",
                (byte)1);
    }

    @Test
    public void returnByteConstant() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 100;" +
                        "}" +
                        "}",
                (byte)100);
    }

    @Test
    public void returnShortConstant() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1000;" +
                        "}" +
                        "}",
                (short)1000);
    }

    @Test
    public void returnIntConstant() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 100000;" +
                        "}" +
                        "}",
                100000);
    }

    @Test
    public void returnLongConstant() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 10000000000;" +
                        "}" +
                        "}",
                10000000000L);
    }

    @Test
    public void returnFactorTest1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 * 3;" +
                        "}" +
                        "}",
                (byte)6);
    }

    @Test
    public void returnFactorTest2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 6 / 2;" +
                        "}" +
                        "}",
                (byte)3);
    }

    @Test
    public void returnAddByte() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 + 3;" +
                        "}" +
                        "}",
                (byte)5);
    }

    @Test
    public void returnSubByte() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 - 3;" +
                        "}" +
                        "}",
                (byte)-1);
    }

    @Test
    public void returnAddShort() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1000 + 3;" +
                        "}" +
                        "}",
                (short)1003);
    }

    @Test
    public void returnSubShort() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1000 - 3;" +
                        "}" +
                        "}",
                (short)997);
    }

    @Test
    public void returnAddInteger() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 100000 + 3;" +
                        "}" +
                        "}",
                100003);
    }

    @Test
    public void returnSubInteger() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 100003 - 3;" +
                        "}" +
                        "}",
                100000);
    }

    @Test
    public void returnAddLong() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1000000000000 + 3;" +
                        "}" +
                        "}",
                1000000000003L);
    }

    @Test
    public void returnSubLong() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 10000000000003 - 3;" +
                        "}" +
                        "}",
                10000000000000L);
    }

    @Test
    public void returnVariable() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "long a = 1;" +
                        "long b = a;" +
                        "return b;" +
                        "}" +
                        "}",
                1L);
    }

    @Test
    public void returnVarVariable() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "var a = 1000;" +
                        "return a;" +
                        "}" +
                        "}",
                (short)1000);
    }

    @Test
    public void returnAssigned() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 1000;" +
                        "a = 10;" +
                        "return a;" +
                        "}" +
                        "}",
                10);
    }

    @Test
    public void returnBooleanConstant1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return false;" +
                        "}" +
                        "}",
                false);
    }

    @Test
    public void returnBooleanConstant2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return true;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnBooleanVariable() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "var a = false;" +
                        "return a;" +
                        "}" +
                        "}",
                false);
    }

    @Test
    public void returnLess() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 1 < 2;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnLessOrEqual() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 <= 2;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnGreater() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 > 1;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnGreaterOrEqual() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 >= 2;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnEqual1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 == 2;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnEqual2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 == 3;" +
                        "}" +
                        "}",
                false);
    }

    @Test
    public void returnNotEqual1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 != 3;" +
                        "}" +
                        "}",
                true);
    }

    @Test
    public void returnNotEqual2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "return 2 != 2;" +
                        "}" +
                        "}",
                false);
    }

    @Test
    public void ifStmt1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "if (1<2) {" +
                        "a = 1;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                1);
    }

    @Test
    public void ifStmt2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "if (1>2) {" +
                        "a = 1;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                0);
    }

    @Test
    public void ifElseStmt1() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "if (1>2) {" +
                        "a = 1;" +
                        "} else {" +
                        "a = 3;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                3);
    }

    @Test
    public void ifElseStmt2() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "if (1<2) {" +
                        "a = 1;" +
                        "} else {" +
                        "a = 3;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                1);
    }

    @Test
    public void ifElseStmt3() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 1;" +
                        "if (a > 0) {" +
                        "a = 10;" +
                        "} else if (a == 0) {" +
                        "a = 11;" +
                        "} else {" +
                        "a = 12;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                10);
    }

    @Test
    public void ifElseStmt4() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "if (a > 0) {" +
                        "a = 10;" +
                        "} else if (a == 0) {" +
                        "a = 11;" +
                        "} else {" +
                        "a = 12;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                11);
    }

    @Test
    public void ifElseStmt5() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = -1;" +
                        "if (a > 0) {" +
                        "a = 10;" +
                        "} else if (a == 0) {" +
                        "a = 11;" +
                        "} else {" +
                        "a = 12;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                12);
    }

    @Test
    public void whileStmt() {
        returnValueTest("class Main {" +
                        "main() {" +
                        "int a = 0;" +
                        "while (a < 10) {" +
                        "a = a + 1;" +
                        "}" +
                        "return a;" +
                        "}" +
                        "}",
                10);
    }

    private void returnValueTest(String code, Object expected) {
        try {
            byte[] compiledBytes = NfCompiler.compile(code);
            if (compiledBytes != null) {
//                Files.write(Paths.get("Main.class"), compiledBytes);
                Class<?> clazz = new TestClassLoader().defineClass("Main", compiledBytes);
                Method printMethod = clazz.getDeclaredMethod("main");
                Object obj = clazz.getConstructor().newInstance();
                Object result = printMethod.invoke(obj);

                Assertions.assertEquals(expected, result);


            } else {
                Assertions.fail("Compiler returned null for some reason");
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            Assertions.fail("Got eception:" + e.getMessage());
        }
    }
}
