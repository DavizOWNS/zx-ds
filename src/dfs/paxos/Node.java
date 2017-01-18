package dfs.paxos;

import dfs.management.IManager;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Node<TVal extends IValue, TService extends Remote> extends UnicastRemoteObject implements INode<TVal, TService>{

    private String guid;
    private IAcceptor<TVal> mAcceptor;
    private IProposer<TVal, TService> mProposer;
    private IManager<TVal, TService> mManager;

    public Node(String guid, IManager<TVal, TService> manager) throws RemoteException {
        super();
        this.guid = guid;
        mAcceptor = new Acceptor<>(this);
        mProposer = new Proposer<>(this);
        mManager = manager;
    }

    @Override
    public PrepareResponse<TVal> prepare(int agreementInstance, int id) throws RemoteException {
        return mAcceptor.prepare(agreementInstance, id);
    }

    @Override
    public boolean accept(int agreementInstance, int id, TVal value) throws RemoteException {
        return mAcceptor.accept(agreementInstance, id, value);
    }

    @Override
    public void decide(int agreementInstance, TVal value) throws RemoteException {
        mAcceptor.decide(agreementInstance, value);
    }

    @Override
    public void run(int agreementInstance, List<INode<TVal, TService>> nodes, TVal value) throws RemoteException{
        mProposer.run(agreementInstance, nodes, value);
    }

    @Override
    public void hearthbeat(String fromId) {
        System.out.println("Hearthbeat: " + fromId + " -> " + getGuid());
    }

    @Override
    public String getGuid() {
        return guid;
    }

    public IManager<TVal, TService> getManager() {
        return mManager;
    }
}
