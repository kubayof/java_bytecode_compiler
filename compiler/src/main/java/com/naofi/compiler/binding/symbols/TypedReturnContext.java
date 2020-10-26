package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.ArrayList;
import java.util.List;

public class TypedReturnContext extends NfLangParser.ReturnContext {
    private final List<VariableType> types = new ArrayList<>();
    private final List<NfLangParser.ExpressionContext> expressions = new ArrayList<>();

    public TypedReturnContext(NfLangParser.ReturnContext original, List<VariableType> types) {
        super(original);
        this.types.addAll(types);
        expressions.addAll(original.expression());
    }

    public List<VariableType> getTypes() {
        return types;
    }

    public List<NfLangParser.ExpressionContext> getExpressions() {
        return expressions;
    }
}
