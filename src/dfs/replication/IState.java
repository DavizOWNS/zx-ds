package dfs.replication;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by DAVID on 21.1.2017.
 */
public interface IState extends Remote{
    Object getState() throws RemoteException;
    void setState(Object state) throws RemoteException;
}
