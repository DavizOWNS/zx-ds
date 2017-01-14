package dfs.paxos;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface INode<TVal extends IValue> extends IAcceptor<TVal>, IProposer<TVal> {
    void hearthbeat();
    String getGuid();
}
