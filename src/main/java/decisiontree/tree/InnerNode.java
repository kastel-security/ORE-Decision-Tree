package decisiontree.tree;


import decisiontree.data.Data;

import java.util.Objects;

public class InnerNode<L> extends Node<L> {

    public final Node<L> leftNode;
    public final Node<L> rightNode;
    public final Condition condition;

    public InnerNode(Node<L> leftNode, Node<L> rightNode, Condition condition) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.condition = condition;
    }

    @Override
    public L classify(Data data) {
        return (condition.test(data) ? leftNode : rightNode).classify(data);
    }

    @Override
    protected <StateIn, StateOut> StateOut visit(TreeVisitor<L, StateIn, StateOut> visitor, StateIn stateIn) {
        return visitor.visitInner(this, stateIn);
    }

    @Override
    public String toString() {
        return "<inner>" + condition + leftNode + rightNode + "</inner>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InnerNode<?> innerNode = (InnerNode<?>) o;
        return leftNode.equals(innerNode.leftNode) && rightNode.equals(innerNode.rightNode) && condition.equals(innerNode.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftNode, rightNode, condition);
    }
}
