package decisiontree.pruning;

import decisiontree.data.Dataset;
import decisiontree.tree.Node;

public interface PruningLogic {
    <L> Node<L> getNode(Node<L> originalNode, Dataset<L> dataset);
}
