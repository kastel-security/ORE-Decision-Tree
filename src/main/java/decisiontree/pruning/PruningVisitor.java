package decisiontree.pruning;

import decisiontree.data.Dataset;
import decisiontree.tree.*;

public class PruningVisitor<L> extends TreeVisitor<L, Dataset<L>, Node<L>> {

    private final PruningLogic logic;

    public PruningVisitor(PruningLogic logic) {
        this.logic = logic;
    }

    @Override
    protected Node<L> visitLeaf(LeafNode<L> node, Dataset<L> dataset) {
        return node;
    }

    @Override
    protected Node<L> visitInner(InnerNode<L> node, Dataset<L> dataset) {
        var res = logic.getNode(node, dataset);
        if (res != null) {
            return res;
        }
        var partitionedSet = dataset.splitAt(node.condition);
        var leftData = partitionedSet.getFirst();
        var rightData = partitionedSet.getSecond();
        if (leftData.getDataSize() == 0) {
            return visit(node.rightNode, dataset);
        } else if (rightData.getDataSize() == 0) {
            return visit(node.leftNode, dataset);
        } else {
            var newLeft = visit(node.leftNode, leftData);
            var newRight = visit(node.rightNode, rightData);
            if (newLeft == node.leftNode && newRight == node.rightNode) {
                return node;
            } else {
                return new InnerNode<>(newLeft, newRight, node.condition);
            }
        }
    }

    @Override
    protected Node<L> visitForest(ForestNode<L> node, Dataset<L> dataset) {
        var originalNodes = node.getSubNodes();
        Node<L>[] ret = new Node[originalNodes.size()];
        for (int i = 0; i < originalNodes.size(); i++) {
            ret[i] = visit(originalNodes.get(i), dataset);
        }
        return new ForestNode<>(ret);
    }
}
