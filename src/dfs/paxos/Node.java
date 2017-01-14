package dfs.paxos;

import dfs.management.IManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Node<TVal extends IValue> implements INode<TVal>{

    private String guid;
    private IAcceptor<TVal> mAcceptor;
    private IProposer<TVal> mProposer;
    private IManager<TVal> mManager;

    public Node(String guid, IManager<TVal> manager)
    {
        this.guid = guid;
        mAcceptor = new Acceptor<>(this);
        mProposer = new Proposer<>(this);
        mManager = manager;
    }

    @Override
    public PrepareResponse<TVal> prepare(int agreementInstance, int id) {
        return mAcceptor.prepare(agreementInstance, id);
    }

    @Override
    public boolean accept(int agreementInstance, int id, TVal value) {
        return mAcceptor.accept(agreementInstance, id, value);
    }

    @Override
    public void decide(int agreementInstance, TVal value) {
        mAcceptor.decide(agreementInstance, value);
        mManager.paxosCommit(agreementInstance, value);
    }

    @Override
    public void run(int agreementInstance, List<INode<TVal>> nodes, TVal value) {
        mProposer.run(agreementInstance, nodes, value);
    }

    @Override
    public void hearthbeat() {

    }

    @Override
    public String getGuid() {
        return guid;
    }
}
