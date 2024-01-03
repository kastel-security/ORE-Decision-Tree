package decisiontree.pruning;

import decisiontree.data.Dataset;
import decisiontree.tree.LeafNode;
import decisiontree.tree.Node;

public class AccuracyPruning implements PruningLogic {
    private final double requiredAccuracy;

    public AccuracyPruning(double requiredAccuracy) {
        this.requiredAccuracy = requiredAccuracy;
    }

    @Override
    public <L> Node<L> getNode(Node<L> originalNode, Dataset<L> dataset) {
        var counter = dataset.getLabelCounter();
        var highest = counter.getMax();
        var prob = ((double) counter.getCount(highest)) / dataset.getDataSize();
        if (prob >= requiredAccuracy) {
            return new LeafNode<>(highest);
        }
        return null;
    }
}
