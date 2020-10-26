package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

public class TypedOp1Context extends NfLangParser.Op1Context {
    private final VariableType type;
    private final char operator;

    public TypedOp1Context(NfLangParser.Op1Context original, VariableType type) {
        super(original.getParent(), original.invokingState);
        this.type = type;
        this.operator = original.getText().charAt(0);
    }

    public VariableType getType() {
        return type;
    }

    public char getOperator() {
        return operator;
    }
}
