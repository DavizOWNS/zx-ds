package dfs.paxos;

import dfs.replication.IState;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IProposer<T extends IValue, TService extends IState> extends Serializable, Remote {
    void run(int agreementInstance, List<INode<T, TService>> nodes, T value) throws RemoteException;
}
