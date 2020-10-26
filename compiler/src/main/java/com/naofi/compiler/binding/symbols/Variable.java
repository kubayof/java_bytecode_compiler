package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.Objects;

public class Variable extends NfLangParser.VariableContext {
    private final String name;
    private final VariableType type;
    private boolean isInitialized = false;

    Variable(NfLangParser.VariableContext context, VariableType type) {
        super(context.getParent(), context.invokingState);
        name = context.IDENTIFIER().getText();
        this.type = type;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public final String getName() {
        return name;
    }

    public final VariableType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
