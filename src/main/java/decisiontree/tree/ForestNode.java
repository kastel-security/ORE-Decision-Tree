package decisiontree.tree;

import decisiontree.data.Data;
import decisiontree.util.Counter;

import java.util.Arrays;
import java.util.List;

public class ForestNode<L> extends Node<L> {

    private final Node<L>[] nodes;

    public ForestNode(Node<L>[] nodes) {
        this.nodes = nodes;
    }

    @Override
    public L classify(Data data) {
        var counter = new Counter<L>();
        for (var node : nodes) {
            counter.add(node.classify(data));
        }
        return counter.getMax();
    }

    @Override
    protected <StateIn, StateOut> StateOut visit(TreeVisitor<L, StateIn, StateOut> visitor, StateIn stateIn) {
        return visitor.visitForest(this, stateIn);
    }

    public List<Node<L>> getSubNodes() {
        return Arrays.asList(nodes);
    }

    @Override
    public String toString() {
        var ret = "<forest>";
        for (var node : nodes) {
            ret += node;
        }
        return ret + "</forest>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForestNode<?> that = (ForestNode<?>) o;
        return Arrays.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nodes);
    }
}
