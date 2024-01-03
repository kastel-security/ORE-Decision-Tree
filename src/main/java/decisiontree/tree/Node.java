package decisiontree.tree;

import decisiontree.data.Data;

import java.io.Serializable;

public abstract class Node<L> implements Serializable {

    public abstract L classify(Data data);

    protected abstract <StateIn, StateOut> StateOut visit(TreeVisitor<L, StateIn, StateOut> visitor, StateIn state);

}
