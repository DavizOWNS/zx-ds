package dfs.test;

import dfs.dfsservice.LockCacheImpl;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by DAVID on 21.1.2017.
 */
public class LockCacheClientTestMain {
    public static void main(String[] args) {
        int port = Integer.valueOf(args[0]);

        try {
            ReplicatedLockConnectorProxy proxy = new ReplicatedLockConnectorProxy(port);
            LockCacheImpl client = new LockCacheImpl(proxy);

            client.acquire("/home");
            client.release("/home");
            client.doRelease();

            System.in.read();

            client.acquire("/home");
            client.release("/home");
            client.doRelease();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        try {
            System.in.read();
        }
        catch (Exception ex)
        {

        }
    }
}
