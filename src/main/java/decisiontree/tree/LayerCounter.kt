package decisiontree.tree

import kotlin.math.max

class LayerCounter<L>: TreeVisitor<L, Unit, Int>() {
    override fun visitLeaf(node: LeafNode<L>, state: Unit): Int = 1

    override fun visitInner(node: InnerNode<L>, state: Unit): Int {
        return max(visit(node.leftNode, state), visit(node.rightNode, state)) + 1
    }

    override fun visitForest(node: ForestNode<L>, state: Unit): Int {
        return node.subNodes.stream().mapToInt { visit(it, state) }.max().asInt + 1
    }
}