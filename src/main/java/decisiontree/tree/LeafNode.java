package decisiontree.tree;

import decisiontree.data.Data;

import java.util.Objects;

public class LeafNode<L> extends Node<L> {

    private final L label;

    public LeafNode(L label) {
        this.label = label;
    }

    @Override
    public L classify(Data data) {
        return label;
    }

    @Override
    protected <StateIn, StateOut> StateOut visit(TreeVisitor<L, StateIn, StateOut> visitor, StateIn stateIn) {
        return visitor.visitLeaf(this, stateIn);
    }

    @Override
    public String toString() {
        return "<leaf label=\"" + label + "\" />";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeafNode<?> leafNode = (LeafNode<?>) o;
        return label.equals(leafNode.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
