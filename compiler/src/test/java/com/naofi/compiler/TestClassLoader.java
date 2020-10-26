package com.naofi.compiler;

public class TestClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
