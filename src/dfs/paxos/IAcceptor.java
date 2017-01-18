package dfs.paxos;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IAcceptor<T extends IValue> extends Serializable, Remote{
    PrepareResponse<T> prepare(int agreementInstance, int id) throws RemoteException;

    boolean accept(int agreementInstance, int id, T value) throws RemoteException;

    void decide(int agreementInstance, T value) throws RemoteException;
}
