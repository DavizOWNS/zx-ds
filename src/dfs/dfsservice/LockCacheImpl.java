package dfs.dfsservice;

import dfs.lockservice.LockConnector;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/**
 * Created by DAVID on 22.10.2016.
 */
public class LockCacheImpl implements dfs.dfsservice.LockCache, LockCacheConnector, Serializable {
    private final String REGISTRY_NAME = "LockCacheService";
    private String ID = UUID.randomUUID().toString();
    private String CLIENT_ID;
    private static Registry registry;

    private static final Set<String> locks = new HashSet<>();
    private static final Set<String> cachedLocks = new HashSet<>();
    private static final Dictionary<Long, String> pendingLocks = new Hashtable<>();
    private static final Set<String> pendingRevokes = new HashSet<>();
    private LockConnector lockServer;
    private int port;
    private static long nextId;

    public LockCacheImpl(LockConnector lockConnector)
    {
        this.port = 0;
        lockServer = lockConnector;
        CLIENT_ID = "127.0.0.1:" + port + ":" + UUID.randomUUID();
    }
    public LockCacheImpl(int port, LockConnector lockServer) throws RemoteException
    {
        this.port = port;
        //locks= new HashSet<>();
        //pendingRevokes = new HashSet<>();
        //cachedLocks = new HashSet<>();
        //pendingLocks = new Hashtable<>();
        lockServer = lockServer;

        exportAndBind();

        CLIENT_ID = "127.0.0.1:" + port + ":" + UUID.randomUUID();
    }


    private void exportAndBind() throws RemoteException
    {
        System.out.println("Starting " + REGISTRY_NAME + " at port " + port);
        System.out.println("Retrieving registry");
        registry = LocateRegistry.getRegistry(port);

        System.out.println("Binding...");
        try {
            registry.bind(REGISTRY_NAME, this);
        } catch (AlreadyBoundException e) {
            System.out.println("Already bound");
            e.printStackTrace();
        }

        System.out.println("Object " + REGISTRY_NAME + " bound to registry at port " + port);
    }

    /**
     * Acquire lock from the cache.
     * @param lockId Lock identifier
     */
    @Override
    public void acquire(String lockId) {
        System.out.print("LockCache[" + ID + "]: Acquire lock: " + lockId + "\n");

        synchronized (cachedLocks){
            if(cachedLocks.contains(lockId)){
                cachedLocks.remove(lockId);
                locks.add(lockId);
                return;
            }
        }
        if(locks.contains(lockId)) return;
        try {
            long seq = nextId++;
            if(lockServer.acquire(lockId, CLIENT_ID, seq)){
                System.out.println("Acquire succesfull");
                locks.add(lockId);
                return;
            } else {
                System.out.println("Acquire failed");
                pendingLocks.put(seq, lockId);
                while(pendingLocks.get(seq) != null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * Release lock from the cache.
     * @param lockId Lock identifier
     */
    @Override
    public void release(String lockId) {
        System.out.print("LockCache[" + ID + "]: Release lock: " + lockId + "\n");

        synchronized (pendingRevokes){
            if(pendingRevokes.contains(lockId)){
                pendingRevokes.remove(lockId);

                if(locks.contains(lockId)) locks.remove(lockId);
                try {
                    lockServer.release(lockId, CLIENT_ID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        synchronized (locks) {
            if (locks.contains(lockId))
                locks.remove(lockId);
        }
        synchronized (cachedLocks) {
            if (!cachedLocks.contains(lockId))
                cachedLocks.add(lockId);
        }
    }

    /**
     * Perform all requested releases of locks from the lock service.
     */
    @Override
    public void doRelease() {
        System.out.print("LockCache[" + ID + "]: doRelease" + "\n");

        synchronized (cachedLocks){
            for(String lockId : cachedLocks){
                try {
                    lockServer.release(lockId, CLIENT_ID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Request to revoke the unused lock from the cache.
     * @param lockId Lock identifier
     * @throws RemoteException
     */
    @Override
    public void revoke(String lockId) throws RemoteException {
        System.out.print("LockCache[" + ID + "]: Revoke lock: " + lockId + "\n");

        /*//TODO
        System.out.println("LockCache: Revoke lock: " + cachedLocks.contains(lockId));
        return;*/

        //synchronized (cachedLocks){
            if(cachedLocks.contains(lockId)){
                cachedLocks.remove(lockId);
                new Thread(() -> {
                    try {
                        lockServer.release(lockId, CLIENT_ID);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                if(!pendingRevokes.contains(lockId))
                    pendingRevokes.add(lockId);
            }
        //}
    }

    /**
     * Request to retry acquiring the lock from the Lock Service.
     * @param lockId Lock identifier
     * @param sequence Sequence number of the lock request.
     * @throws RemoteException
     */
    @Override
    public void retry(String lockId, long sequence) throws RemoteException {
        System.out.print("LockCache[" + ID + "]: Retry lock: " + lockId + "\n");

        if(lockServer.acquire(lockId, CLIENT_ID, sequence)){
            synchronized (pendingLocks){
                pendingLocks.remove(sequence);
            }
        }
    }

    @Override
    public void stop(){
        try {
            registry.unbind(REGISTRY_NAME);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }
}
