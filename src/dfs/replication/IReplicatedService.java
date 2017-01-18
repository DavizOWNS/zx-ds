package dfs.replication;

import dfs.management.IManager;
import dfs.management.Manager;
import dfs.management.View;
import dfs.util.IAction;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by DAVID on 16.1.2017.
 */
public interface IReplicatedService<TService extends Remote> extends Remote, Serializable {
    <TRes extends Serializable> TRes useService(IAction<TRes, TService> action) throws RemoteException;
    IManager<View<TService>, TService> getManager() throws RemoteException;
}
