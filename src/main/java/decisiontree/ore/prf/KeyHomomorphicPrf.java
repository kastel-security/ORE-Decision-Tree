package decisiontree.ore.prf;

import decisiontree.math.Group;

import java.math.BigInteger;

public interface KeyHomomorphicPrf<V> extends Prf<BigInteger, V> {
    Group<V> getGroup();
}
