package decisiontree.tree;

public abstract class TreeVisitor<L, StateIn, StateOut> {

    public final StateOut visit(Node<L> node, StateIn state) {
        return node.visit(this, state);
    }

    protected abstract StateOut visitLeaf(LeafNode<L> node, StateIn state);
    protected abstract StateOut visitInner(InnerNode<L> node, StateIn state);
    protected abstract StateOut visitForest(ForestNode<L> node, StateIn state);
}
