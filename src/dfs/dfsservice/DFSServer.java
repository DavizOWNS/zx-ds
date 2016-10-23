package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;
import dfs.lockservice.LockConnector;

import java.io.File;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dávid on 1.10.2016.
 */
public class DFSServer implements dfs.dfsservice.DFSConnector, Serializable {

    private final String REGISTRY_NAME = "DFSService";
    private static Registry registry;

    private final int mPort;
    private final ExtentConnector mExtentConnector;
    private final LockCache lockCache;

    public DFSServer(int port, ExtentConnector extentConnector, LockConnector lockConnector) throws RemoteException
    {
        mPort = port;
        mExtentConnector = extentConnector;

        exportAndBind();
        lockCache = new LockCacheImpl(port, lockConnector);
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
    public List<String> dir(String path) throws RemoteException {
        System.out.println("Retrieving dir for " + path);
        byte[] dirContents = mExtentConnector.get(path);
        if(dirContents == null)
            return null;
        List<String> result = new ArrayList<>();
        String contents = new String(dirContents);
        for(String val :contents.split(","))
        {
            result.add(val);
        }

        return result;
    }

    @Override
    public boolean mkdir(String path) throws RemoteException {
        String parentDir = new File(path).getParent();
        lockCache.acquire(parentDir);
        boolean result = mExtentConnector.put(path, new byte[] {});
        lockCache.release(parentDir);

        return result;
    }

    @Override
    public boolean rmdir(String path) throws RemoteException {
        String lockId = path;
        lockCache.acquire(lockId);
        boolean result = mExtentConnector.put(path, null);
        lockCache.release(lockId);
        return result;
    }

    @Override
    public byte[] get(String path) throws RemoteException {
        return mExtentConnector.get(path);
    }

    @Override
    public boolean put(String path, byte[] bytes) throws RemoteException {
        String lockId = path;
        lockCache.acquire(lockId);
        boolean result = mExtentConnector.put(path, bytes);
        lockCache.release(lockId);
        return result;
    }

    @Override
    public boolean delete(String path) throws RemoteException {
        String lockId = path;
        lockCache.acquire(lockId);
        boolean result = mExtentConnector.put(path, null);
        lockCache.release(lockId);
        return result;
    }

    @Override
    public void stop() throws RemoteException {
        try {
            registry.unbind(REGISTRY_NAME);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        lockCache.stop();
    }
}
