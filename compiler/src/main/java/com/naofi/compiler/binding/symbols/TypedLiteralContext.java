package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

public class TypedLiteralContext extends NfLangParser.LiteralContext {
    private final VariableType type;
    private final long value;

    public TypedLiteralContext(NfLangParser.LiteralContext context) {
        super(context.getParent(), context.invokingState);
        value = Long.parseLong(context.getText());
        type = VariableType.fitsRange(value);
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
