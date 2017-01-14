package dfs.paxos;

import java.util.List;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IProposer<T extends IValue> {
    void run(int agreementInstance, List<INode<T>> nodes, T value);
}
