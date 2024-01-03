package decisiontree.pruning;

import decisiontree.data.Dataset;
import decisiontree.tree.LeafNode;
import decisiontree.tree.Node;

public class SizePruning implements PruningLogic {

    private final int sizeTresh;

    public SizePruning(int sizeTresh) {
        this.sizeTresh = sizeTresh;
    }

    @Override
    public <L> Node<L> getNode(Node<L> originalNode, Dataset<L> dataset) {
        if (dataset.getDataSize() < sizeTresh) {
            return new LeafNode<>(dataset.getLabelCounter().getMax());
        }
        return null;
    }
}
