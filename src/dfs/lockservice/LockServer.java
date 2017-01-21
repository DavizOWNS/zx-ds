package dfs.lockservice;

import dfs.dfsservice.LockCache;
import dfs.dfsservice.LockCacheConnector;
import dfs.replication.IState;
import dfs.replication.ServiceActionResult;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * Created by Dávid on 1.10.2016.
 */
public class LockServer implements ILockConnectorWithState, Serializable {
    private final String REGISTRY_NAME = "LockService";
    private final String ID = "LockServer["+UUID.randomUUID().toString()+"]";
    private static Registry registry;

    private final int mPort;
    private State state;

    public LockServer()
    {
        mPort = 0;
        state = new State();
    }
    public LockServer(int port) throws RemoteException
    {
        mPort = port;
        state = new State();
        //acquiredLocks = new Hashtable<>();
        //requestedLocks = new ArrayList<>();

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
        System.out.print(ID +": Acquire lock: " + lockId + " for " + ownerId + "\n");
        Lock requestedLock = new Lock(lockId, ownerId, sequence);
        synchronized (state.acquiredLocks){
            Lock existingLock = findLock(requestedLock.getLockId());
            if(existingLock == null || existingLock.getOwnerId().equals(requestedLock.getOwnerId()))
            {
                state.acquiredLocks.add(requestedLock);
                return true;
            }
            else
            {
                state.requestedLocks.add(requestedLock);
                String[] parts = existingLock.getOwnerId().split(":");
                LockCacheConnector cacheConnector = locateLockCacheConnector(Integer.parseInt(parts[1]));
                if(cacheConnector != null) {
                    cacheConnector.revoke(existingLock.getLockId());
                }
                else System.out.print("Failed to locate LockCacheConnector" + "\n");

                return false;
            }
        }
    }

    @Override
    public void release(String lockId, String ownerId) throws RemoteException {
        System.out.print(ID + ": Release lock: " + lockId + " for " + ownerId + "\n");
        synchronized (state.acquiredLocks){
            System.out.println("Removing lock " + lockId);
            state.acquiredLocks.remove(findExact(lockId));
        }
        synchronized (state.requestedLocks){
            for (int i = state.requestedLocks.size() - 1; i >=0; i--){
                Lock lock = state.requestedLocks.get(i);
                if(!lock.getLockId().equals(lockId) && !isChild(lockId, lock.getLockId())) continue;

                //call retry ?
                String[] parts = lock.getOwnerId().split(":");
                LockCacheConnector cacheConnector = locateLockCacheConnector(Integer.parseInt(parts[1]));
                if(cacheConnector != null) {
                    cacheConnector.retry(lock.getLockId(), lock.getSequenceId());
                }
                else System.out.print("Failed to locate LockCacheConnector" + "\n");

                state.requestedLocks.remove(i);
            }
        }
    }

    private Lock findExact(String lockId)
    {
        for(Lock lock : state.acquiredLocks)
        {
            if(lock.getLockId().equals(lockId))
                return lock;
        }

        return null;
    }
    private boolean isChild(String parent, String other)
    {
        if(Objects.equals(parent, "/") || Objects.equals(parent, "\\"))
            return true;

        return parent.startsWith(other);
    }
    private Lock findLock(String lockId)
    {
        Lock result = null;
        for(Lock lock : state.acquiredLocks)
        {
            if(lock.getLockId().equals("\\") || lock.getLockId().equals("/"))
                return lock;
            if(lock.getLockId().equals(lockId))
                return lock;
            if(lock.getLockId().endsWith("/"))
            {
                if(lockId.startsWith(lock.getLockId()))
                    return lock;
            }
        }

        return result;
    }

    private LockCacheConnector locateLockCacheConnector(int clientPort) throws RemoteException{
        Registry reg = LocateRegistry.getRegistry(clientPort);
        if(reg == null) return null;
        try {
            LockCacheConnector cacheConnector = (LockCacheConnector) reg.lookup("LockCacheService");

            return cacheConnector;
        } catch (NotBoundException e) {
            e.printStackTrace();

            return null;
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

    @Override
    public Object getState() throws RemoteException {
        return state;
    }

    @Override
    public void setState(Object state) throws RemoteException {
        System.out.println("Received new state:\n\tacquiredLocks: " + ((State)state).acquiredLocks.size() + "\n\toldLockCount: " + this.state.acquiredLocks.size());
        this.state = (State)state;
    }

    private class State implements Serializable
    {
        List<Lock> acquiredLocks = new ArrayList<>();
        List<Lock> requestedLocks = new ArrayList<>();
    }
}
