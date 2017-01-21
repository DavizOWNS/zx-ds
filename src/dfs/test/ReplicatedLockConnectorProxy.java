package dfs.test;

import dfs.lockservice.ILockConnectorWithState;
import dfs.lockservice.LockConnector;
import dfs.replication.IReplicatedService;
import dfs.replication.ServiceActionResult;
import dfs.util.IAction;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by DAVID on 21.1.2017.
 */
public class ReplicatedLockConnectorProxy implements LockConnector, Serializable {
    private IReplicatedService<ILockConnectorWithState> service;
    private List<IReplicatedService<ILockConnectorWithState>> members;

    public ReplicatedLockConnectorProxy(int servicePort) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(servicePort);
        service = (IReplicatedService<ILockConnectorWithState>) registry.lookup("replica");

        members = service.members();
    }

    @Override
    public boolean acquire(String s, String s1, long l) throws RemoteException {
        ServiceActionResult<Boolean> res = null;
        try {
            res = service.useService(new IAction<Boolean, ILockConnectorWithState>() {
                @Override
                public Boolean execute(ILockConnectorWithState obj) throws RemoteException {
                    return obj.acquire(s, s1, l);
                }
            });
        }
        catch (RemoteException ex)
        {
            ex.printStackTrace();
            updateService();
            return acquire(s, s1, l);
        }

        if(res.getState() == ServiceActionResult.State.NOTPRIMARY)
        {
            updateService();
            return acquire(s, s1, l);
        }

        return res.getResult();
    }

    @Override
    public void release(String s, String s1) throws RemoteException {
        ServiceActionResult<Boolean> res = null;
        try {
            res = service.useService(new IAction<Boolean, ILockConnectorWithState>() {
                @Override
                public Boolean execute(ILockConnectorWithState obj) throws RemoteException {
                    obj.release(s, s1);
                    return true;
                }
            });
        }
        catch (RemoteException ex)
        {
            ex.printStackTrace();
            updateService();
            release(s, s1);
            return;
        }

        if(res.getState() == ServiceActionResult.State.NOTPRIMARY)
        {
            updateService();
            release(s, s1);
            return;
        }
    }

    private void updateService() throws RemoteException {
        try {
            service = members.get(0);
            members = service.members();
        }
        catch (RemoteException ex)
        {
            if(members.size() == 1) //no members left, all died
                throw ex;
            members.remove(0);
            updateService();
        }
    }

    @Override
    public void stop() throws RemoteException {
        //client can not stop service
    }
}
