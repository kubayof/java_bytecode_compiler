package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.ArrayList;
import java.util.List;

public class TypedMethodContext extends NfLangParser.MethodContext {
    private final String name;
    private final List<VariableType> formalTypes = new ArrayList<>();
    private final List<VariableType> returnTypes = new ArrayList<>();
    private final NfLangParser.BlockContext body;

    public TypedMethodContext(NfLangParser.MethodContext original,
                              List<VariableType> formalTypes, List<VariableType> returnTypes) {
        super(original.getParent(), original.invokingState);
        name = original.IDENTIFIER().getText();
        this.formalTypes.addAll(formalTypes);
        this.returnTypes.addAll(returnTypes);
        body = original.block();
    }

    public final String getName() {
        return name;
    }

    public final List<VariableType> getFormalTypes() {
        return new ArrayList<>(formalTypes);
    }

    public final List<VariableType> getReturnTypes() {
        return new ArrayList<>(returnTypes);
    }

    public final NfLangParser.BlockContext getBody() {
        return body;
    }
}
