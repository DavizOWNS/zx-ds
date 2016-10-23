package dfs.lockservice;

import dfs.dfsservice.LockCache;
import dfs.dfsservice.LockCacheConnector;

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
public class LockServer implements dfs.lockservice.LockConnector, Serializable {
    private final String REGISTRY_NAME = "LockService";
    private final String ID = "LockServer["+UUID.randomUUID().toString()+"]";
    private static Registry registry;

    private final int mPort;
    private static final Dictionary<String, Lock> acquiredLocks = new Hashtable<>();
    private static final List<Lock> requestedLocks = new ArrayList<>();

    public LockServer(int port) throws RemoteException
    {
        mPort = port;
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
        synchronized (acquiredLocks){
            Lock existingLock = acquiredLocks.get(lockId);
            if(existingLock == null)
            {
                acquiredLocks.put(lockId, requestedLock);
                return true;
            }
            else
            {
                requestedLocks.add(requestedLock);
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
        synchronized (acquiredLocks){
            System.out.println("Removing lock " + lockId);
            acquiredLocks.remove(lockId);
        }
        synchronized (requestedLocks){
            for (int i = requestedLocks.size() - 1; i >=0; i--){
                Lock lock = requestedLocks.get(i);
                if(!lock.getLockId().equals(lockId)) continue;

                //call retry ?
                String[] parts = lock.getOwnerId().split(":");
                LockCacheConnector cacheConnector = locateLockCacheConnector(Integer.parseInt(parts[1]));
                if(cacheConnector != null) {
                    cacheConnector.retry(lock.getLockId(), lock.getSequenceId());
                }
                else System.out.print("Failed to locate LockCacheConnector" + "\n");

                requestedLocks.remove(i);
            }
        }
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
}
