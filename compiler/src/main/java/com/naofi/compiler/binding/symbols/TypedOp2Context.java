package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

public class TypedOp2Context extends NfLangParser.Op2Context {
    private final VariableType type;
    private final char operator;

    public TypedOp2Context(NfLangParser.Op2Context original, VariableType type) {
        super(original.getParent(), original.invokingState);
        this.type = type;
        this.operator = original.getText().charAt(0);
        original.children.forEach(this::addAnyChild);
        original.children.forEach(child -> child.setParent(this));
    }

    public VariableType getType() {
        return type;
    }

    public char getOperator() {
        return operator;
    }
}
