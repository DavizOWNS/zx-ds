package dfs.dfsservice;

import dfs.extentservice.ExtentConnector;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by DAVID on 13.11.2016.
 */
public class ExtentCacheImpl implements ExtentCache, Serializable {

    private String ID = UUID.randomUUID().toString();
    private ExtentConnector extentServer;

    private Dictionary<String, byte[]> files;

    public ExtentCacheImpl(ExtentConnector extentConnector)
    {
        extentServer = extentConnector;
        files = new Hashtable<>();
    }

    @Override
    public byte[] get(String path){
        System.out.print("ExtentCache[" + ID + "]: get: " + path + "\n");

        byte[] result = files.get(path);
        if(result == null)
        {
            try {
                result = extentServer.get(path);
            } catch (RemoteException e) {
                e.printStackTrace();
                return null;
            }

            files.put(path, result);
        }

        return result;
    }

    @Override
    public boolean put(String path, byte[] bytes) {
        System.out.print("ExtentCache[" + ID + "]: put: " + path + "[" + (bytes == null ? "<null>" : bytes.length) + "]" + "\n");

        files.put(path, bytes);

        return true;
    }

    @Override
    public void update(String path) {
        System.out.print("ExtentCache[" + ID + "]: update: " + path + "\n");

    }

    @Override
    public void flush(String path) {
        System.out.print("ExtentCache[" + ID + "]: flush: " + path + "\n");

    }
}
