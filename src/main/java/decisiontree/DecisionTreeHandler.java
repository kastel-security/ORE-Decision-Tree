package decisiontree;

import decisiontree.data.Dataset;
import decisiontree.evaluators.SplitEvaluator;
import decisiontree.pruning.PruningLogic;
import decisiontree.pruning.PruningVisitor;
import decisiontree.tree.*;
import kotlin.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DecisionTreeHandler {

    public static <L> Node<L> train(Dataset<L> dataset, SplitEvaluator evaluator) {
        return train(dataset, evaluator, Integer.MAX_VALUE);
    }
    public static <L> Node<L> train(Dataset<L> dataset, SplitEvaluator evaluator, int maxDepth) {
        assert !dataset.getAllData().isEmpty();

        var counter = dataset.getLabelCounter();

        if (counter.getAllKeys().size() == 1 || maxDepth <= 0) {
            return new LeafNode<>(counter.getMax());
        } else {
            var selectesSplit = evaluator.getOptimalCondition(dataset);
            if (selectesSplit.isEmpty()) {
                return new LeafNode<>(dataset.getLabelCounter().getMax());
            }
            var split = selectesSplit.get();
            var partitionedSets = dataset.splitAt(split);
            if (partitionedSets.getFirst().getAllData().size() == 0 ||
                    partitionedSets.getSecond().getAllData().size() == 0) {
                throw new IllegalStateException("Partitioning did not work. The evaluator selected a bad split.");
            }
            return new InnerNode<>(
                    train(partitionedSets.getFirst(), evaluator, maxDepth - 1),
                    train(partitionedSets.getSecond(), evaluator, maxDepth - 1),
                    split);
        }
    }

    private static <L> Dataset<L>[] partitionRandomly(Dataset<L> dataset, int nSubsets) {
        Dataset<L>[] ret = new Dataset[nSubsets];
        var totalSize = dataset.getDataSize();
        var data = new ArrayList<>(dataset.getAllData());
        for (int i = 0; i < nSubsets; i++) {
            ret[i] = new Dataset<>(new ArrayList<>(totalSize / nSubsets + 1), dataset.getNAttributes());
        }
        var rand = new Random();
        rand.setSeed(42);
        Collections.shuffle(data, rand);
        for (int i = 0; i < data.size(); i++) {
            var ele = data.get(i);
            var targetDataset = i % nSubsets;
            ret[targetDataset].getAllData().add(ele);
        }
        return ret;
    }

    public static <L> Node<L> trainForest(Dataset<L> dataset, SplitEvaluator evaluator, int nTrees) {
        return trainForest(dataset, evaluator, nTrees, Integer.MAX_VALUE);
    }

    public static <L> Node<L> trainForest(Dataset<L> dataset, SplitEvaluator evaluator, int nTrees, int maxDepth) {
        Node<L>[] trees = new Node[nTrees];
        var subsets = partitionRandomly(dataset, nTrees);
        for (int i = 0; i < nTrees; i++) {
            trees[i] = train(subsets[i], evaluator, maxDepth - 1);
        }
        return new ForestNode<>(trees);
    }

    public static <L> double evaluate(Dataset<L> dataset, Node<L> tree) {
        var correct = dataset.getAllData().stream()
                .parallel()
                .filter(entry -> tree.classify(entry.getFirst()).equals(entry.getSecond()))
                .count();
        return ((double) correct) / dataset.getAllData().size();
    }

    public static <L> Node<L> prune(Node<L> node, Dataset<L> pruningData, PruningLogic pl) {
        return new PruningVisitor<L>(pl).visit(node, pruningData);
    }

    public static <L> void printStats(Dataset<L> testData, Node<L> tree) {
        System.out.println("====================\nTree data\n====================");
        System.out.println("Nodes: " + new NodeCounter<L>().visit(tree, Unit.INSTANCE));
        System.out.println("Layers: " + new LayerCounter<L>().visit(tree, Unit.INSTANCE));
        var accuracy = DecisionTreeHandler.evaluate(testData, tree);
        System.out.println("Accuracy: " + accuracy);
        System.out.println();
    }

}
