package dfs.management;

import dfs.paxos.IValue;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IManager<TVal extends IValue> {
    void paxosCommit(int paxosInstance, TVal value);
}
