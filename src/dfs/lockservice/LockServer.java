package dfs.lockservice;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;

/**
 * Created by Dávid on 1.10.2016.
 */
public class LockServer implements dfs.lockservice.LockConnector, Serializable {
    private final String REGISTRY_NAME = "LockService";
    private static Registry registry;

    private final int mPort;
    private HashSet<String> acquiredLocks;

    public LockServer(int port) throws RemoteException
    {
        mPort = port;
        acquiredLocks = new HashSet<>();

        exportAndBind();
    }

    private void exportAndBind() throws RemoteException
    {
        System.out.println("Starting " + REGISTRY_NAME + " at port " + mPort);
        System.out.println("Retrieving registry");
        registry = LocateRegistry.createRegistry(mPort);

        System.out.println("Binding...");
        try {
            registry.bind(REGISTRY_NAME, this);
        } catch (AlreadyBoundException e) {
            System.out.println("Already bound");
            e.printStackTrace();
        }

        System.out.println("Object " + REGISTRY_NAME + " bound to registry at port " + mPort);
    }

    @Override
    public boolean acquire(String lockId, String ownerId, long sequence) throws RemoteException {
        boolean success = false;
        while(!success)
        {
            while(acquiredLocks.contains(lockId)){

            }

            synchronized (acquiredLocks) {
                if (!acquiredLocks.contains(lockId)) {
                    acquiredLocks.add(lockId);
                    success = true;
                }
            }
        }

        return success;
    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
        synchronized (acquiredLocks){
            acquiredLocks.remove(lockId);
        }
    }

    @Override
    public void stop() throws RemoteException {
        try {
            registry.unbind(REGISTRY_NAME);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
