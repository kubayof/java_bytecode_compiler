package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

public class TypedBoolLiteral extends NfLangParser.Bool_termContext {
    private final VariableType type;
    private final boolean value;

    public TypedBoolLiteral(NfLangParser.Bool_termContext original, VariableType type, boolean value) {
        super(original.getParent(), original.invokingState);
        this.type = type;
        this.value = value;
        original.children.forEach(this::addAnyChild);
        original.children.forEach(child -> child.setParent(this));
    }

    public final VariableType getType() {
        return type;
    }

    public final boolean getValue() {
        return value;
    }
}
