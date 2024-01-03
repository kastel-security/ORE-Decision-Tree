package decisiontree.evaluators;

import decisiontree.data.Dataset;
import decisiontree.evaluators.heuristic.Heuristic;
import decisiontree.tree.Condition;
import decisiontree.util.BitComparable;
import decisiontree.util.Counter;
import kotlin.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class TotalHeuristicSplit implements SplitEvaluator {

    protected final Heuristic heuristic;

    public TotalHeuristicSplit(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    private Optional<Pair<Double, Condition>> getBestCondition(Dataset<?> dataset, int attribute) {
        var sortedData = new ArrayList<>(dataset.getAllData());
        var parentCounter = (Counter<Object>)dataset.getLabelCounter();
        if (sortedData.get(0).getFirst().getAttribute(attribute) instanceof BitComparable) {
            BitComparable.msdRadixSortInplaceBy(sortedData, element -> (BitComparable) element.getFirst().getAttribute(attribute));
        } else {
            sortedData.sort(Comparator.comparing(element -> element.getFirst().getAttribute(attribute)));
        }

        var lowerCounter = new Counter<>();
        var upperCounter = new Counter<>();
        for (var ele : sortedData) {
            upperCounter.add(ele.getSecond());
        }

        Comparable bestSplit = null;
        var bestSplitHeuristic = Double.NEGATIVE_INFINITY;

        var previousValue = sortedData.get(0).getFirst().getAttribute(attribute);
        for (var ele : sortedData) {
            var currentValue = ele.getFirst().getAttribute(attribute);
            if (!Objects.equals(previousValue, currentValue)) {
                //The attribute is now at least one larger than previously. New split candidate
                var heuristicValue = heuristic.evaluate(parentCounter, lowerCounter, upperCounter);
                if (heuristicValue > bestSplitHeuristic) {
                    bestSplit = previousValue;
                    bestSplitHeuristic = heuristicValue;
                }
            }
            previousValue = currentValue;
            lowerCounter.add(ele.getSecond());
            upperCounter.remove(ele.getSecond());
        }
        if (bestSplit == null) {
            //No actual split found
            return Optional.empty();
        } else {
            return Optional.of(new Pair<>(bestSplitHeuristic, new Condition(attribute, bestSplit)));
        }
    }

    @Override
    public Optional<Condition> getOptimalCondition(Dataset<?> dataset) {
        return IntStream.range(0, dataset.getNAttributes()).parallel()
                .mapToObj( i -> getBestCondition(dataset, i))
                .filter(opt -> !opt.isEmpty())
                .map(Optional::get)
                .max(Comparator.comparing(Pair::getFirst))
                .map(Pair::getSecond);
    }
}
