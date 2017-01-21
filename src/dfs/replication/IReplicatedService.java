package dfs.replication;

import dfs.management.IManager;
import dfs.management.Manager;
import dfs.management.View;
import dfs.util.IAction;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by DAVID on 16.1.2017.
 */
public interface IReplicatedService<TService extends IState> extends Remote, Serializable {
    <TRes extends Serializable> ServiceActionResult<TRes> useService(IAction<TRes, TService> action) throws RemoteException;
    <TRes extends Serializable> TRes useService(IAction<TRes, TService> action, Viewstamp viewstamp) throws RemoteException;
    IManager<View<TService>, TService> getManager() throws RemoteException;
    ReplicaState getState() throws RemoteException;
    List<IReplicatedService<TService>> members() throws RemoteException;
    Object getServiceState() throws RemoteException;
    void setServiceState(Object state) throws RemoteException;
}
