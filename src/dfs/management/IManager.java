package dfs.management;

import dfs.paxos.INode;
import dfs.paxos.IValue;
import dfs.replication.IReplicatedService;
import dfs.replication.IState;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IManager<TVal extends IValue, TService extends IState> extends Remote, Serializable{
    void addNode(INode<TVal, TService> node) throws RemoteException;
    void paxosCommit(int paxosInstance, TVal value) throws RemoteException;
    IManager<TVal, TService> getMaster() throws RemoteException;
    String getId() throws RemoteException;
    IReplicatedService<TService> getService() throws RemoteException;
}
