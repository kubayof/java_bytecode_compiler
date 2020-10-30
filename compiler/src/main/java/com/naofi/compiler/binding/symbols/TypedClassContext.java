package com.naofi.compiler.binding.symbols;

import com.naofi.antlr.NfLangParser;

import java.util.ArrayList;
import java.util.List;

public class  TypedClassContext extends NfLangParser.ClassDefContext {
    private final String name;
    private final List<NfLangParser.ClassMemberContext> members = new ArrayList<>();

    public TypedClassContext(NfLangParser.ClassDefContext original) {
        super(original.getParent(), original.invokingState);
        name = original.IDENTIFIER().getText();
        members.addAll(original.classMember());
        original.children.forEach(this::addAnyChild);
        original.children.forEach(child -> child.setParent(this));
    }

    public String getName() {
        return name;
    }

    public List<NfLangParser.ClassMemberContext> getMembers() {
        return members;
    }
}
