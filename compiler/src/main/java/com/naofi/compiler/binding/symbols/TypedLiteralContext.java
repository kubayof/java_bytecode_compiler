package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

public class TypedLiteralContext extends NfLangParser.LiteralContext {
    private final VariableType type;
    private final long value;

    public TypedLiteralContext(NfLangParser.LiteralContext original) {
        super(original.getParent(), original.invokingState);
        value = Long.parseLong(original.getText());
        type = VariableType.fitsRange(value);
        original.children.forEach(this::addAnyChild);
        original.children.forEach(child -> child.setParent(this));
    }

    public final VariableType getType() {
        return type;
    }

    public final long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getText();
    }
}
