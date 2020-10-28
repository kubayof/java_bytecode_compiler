package com.naofi.compiler.dfa;

import com.naofi.antlr.NfLangParser;

import java.util.*;
import java.util.function.Consumer;

public class GraphBuilder {
    /**
     * For convenience, can be used in
     */
    protected static class Label {}


    private static class DeferredNode implements Graph.GraphNode {
        private final List<Consumer<Graph.GraphNode>> nodeSetters = new ArrayList<>();

        void addSetter(Consumer<Graph.GraphNode> nodeSetter) {
            nodeSetters.add(nodeSetter);
        }

        void setNode(Graph.GraphNode node) {
            nodeSetters.forEach(setter -> setter.accept(node));
        }
    }

    private final Graph.StartNode start = new Graph.StartNode();
    private final Graph.EndNode end = new Graph.EndNode();
    private Consumer<Graph.GraphNode> nextConsumer = start::setNext;

    private final Map<Label, Graph.GraphNode> labelsNodes = new IdentityHashMap<>();
    private final Map<Label, DeferredNode> labelsDeferredNodes = new IdentityHashMap<>();
    private final List<Label> unboundedLabels = new ArrayList<>();

    /**
     * Connect previous node to end
     */
    public final void end() {
        boundLabels(end);
        nextConsumer.accept(end);
        basicBlock = null;
    }

    /**
     * Insert label
     */
    protected void label(Label label) {
        unboundedLabels.add(label);
        basicBlock = null;
    }

    private Graph.BasicBlock basicBlock;

    /**
     * Add statement to existing block if previous was BasicBlock
     * or create new BasicBlock and add statement to it
     */
    protected void basic(NfLangParser.StatementContext stmt) {
        if (basicBlock == null) {
            basicBlock = new Graph.BasicBlock();
            nextConsumer.accept(basicBlock);
            nextConsumer = basicBlock::setNext;
            boundLabels(basicBlock);
        }
        basicBlock.addStatement(stmt);
    }


    /**
     * Adds conditional jump to graph, if condition evaluates to true then go to true else - go to false
     */
    protected void condJump(NfLangParser.BoolExpressionContext condition, Label ifTrue, Label ifFalse) {
        Graph.ConditionalJump condJump = new Graph.ConditionalJump(condition);
        nextConsumer.accept(condJump);
        boundLabels(condJump);
        setLabelPointer(ifTrue, condJump::setIfTrue);
        setLabelPointer(ifFalse, condJump::setIfFalse);
        basicBlock = new Graph.BasicBlock();
        nextConsumer = basicBlock::setNext;
    }

    protected void goTo(Label label) {
        setLabelPointer(label, basicBlock::setNext);
    }


    /**
     * If block occurred earlier - passes existing block to setter
     * Else if block was not visited yet - creates deferred block or uses existing (if label was used earlier.
     *  Where block -> BasicBlock label points to
     * @param label label pointing to block which is needed to set
     * @param setter setter function
     */
    private void setLabelPointer(Label label, Consumer<Graph.GraphNode> setter) {
        if (labelsNodes.containsKey(label)) {
            setter.accept(labelsNodes.get(label));
            return;
        }
        DeferredNode deferred = labelsDeferredNodes.getOrDefault(label, new DeferredNode());
        deferred.addSetter(setter);
        setter.accept(deferred);
        labelsDeferredNodes.putIfAbsent(label, deferred);
    }

    /**
     * Bounds labels to node and resolves deferred labels if they occured
     * @param node block, labels are pointing to
     */
    private void boundLabels(Graph.GraphNode node) {
        for (Label label : unboundedLabels) {
            labelsNodes.put(label, node);
            if (labelsDeferredNodes.containsKey(label)) {
                DeferredNode deferred = labelsDeferredNodes.get(label);
                deferred.setNode(node);
                labelsNodes.put(label, node);
                labelsDeferredNodes.remove(label);
            }
        }
        unboundedLabels.clear();
    }

    public Graph.StartNode getStartNode() {
        if (!labelsDeferredNodes.isEmpty()) {
            throw new IllegalStateException("There are labels outsize of graph");
        }
        return start;
    }
}
