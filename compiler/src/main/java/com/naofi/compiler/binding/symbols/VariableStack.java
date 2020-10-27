package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.*;

public class VariableStack {
    private final Deque<Map<String, Variable>> stack = new ArrayDeque<>();

    public void pushScope() {
        stack.push(new HashMap<>());
    }

    public void popScope() {
        stack.pop();
    }

    public Variable defineNewVar(NfLangParser.VariableContext context, VariableType type) {
        String name = context.IDENTIFIER().getText();
        Variable var = findVarInScope(name);
        if (var == null) {
            var = new Variable(context, type);
            stack.getFirst().put(name, var);
            return var;
        }

        return null;
    }

    public Variable defineVar(NfLangParser.VariableContext context, VariableType type) {
        String name = context.IDENTIFIER().getText();
        Variable var = findVarInScope(name);
        if (var == null) {
            var = new Variable(context, type);
            stack.getFirst().put(name, var);
        }

        return var;
    }

    public Variable findVar(NfLangParser.VariableContext context) {
        return findVar(context.IDENTIFIER().getText());
    }

    public Variable findVar(String name) {
        return stack.stream()
                .map(scope -> scope.get(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Variable findVarInScope(String name) {
        return stack.getFirst().get(name);
    }
}
