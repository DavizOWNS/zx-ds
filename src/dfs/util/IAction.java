package dfs.util;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by DAVID on 16.1.2017.
 */
public interface IAction<TRes, TParam> extends Remote, Serializable{
    TRes execute(TParam obj) throws RemoteException;
}
