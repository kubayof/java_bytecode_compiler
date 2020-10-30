package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.Objects;

public class Variable extends NfLangParser.VariableContext {
    private final String name;
    private final VariableType type;

    Variable(NfLangParser.VariableContext original, VariableType type) {
        super(original.getParent(), original.invokingState);
        name = original.IDENTIFIER().getText();
        this.type = type;
        original.children.forEach(this::addAnyChild);
        original.children.forEach(child -> child.setParent(this));
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
