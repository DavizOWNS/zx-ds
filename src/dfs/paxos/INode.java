package dfs.paxos;

import dfs.management.IManager;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface INode<TVal extends IValue, TService extends Remote> extends Remote, Serializable, IAcceptor<TVal>, IProposer<TVal, TService> {
    void hearthbeat(String fromId) throws RemoteException;
    String getGuid() throws RemoteException;
    IManager<TVal, TService> getManager() throws RemoteException;
}
