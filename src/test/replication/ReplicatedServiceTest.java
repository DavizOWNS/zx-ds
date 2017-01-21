package test.replication;

import dfs.dfsservice.LockCacheImpl;
import dfs.replication.ReplicatedService;
import dfs.test.ReplicatedLockConnectorProxy;
import org.junit.Test;

import java.rmi.RemoteException;

import static junit.framework.Assert.*;

/**
 * Created by DAVID on 16.1.2017.
 */
public class ReplicatedServiceTest {

/*    @Test
    public void testPaxosWorking() throws RemoteException {
        ReplicatedService<String> masterService = new ReplicatedService<String>(6000, "master");
        ReplicatedService<String> slaveService = new ReplicatedService<String>(6001, "master", 6000);
    }
    @Test
    public void testNodeShutdown() throws RemoteException {
        ReplicatedService<String> masterService = new ReplicatedService<String>(6000, "master");
        ReplicatedService<String> slaveService = new ReplicatedService<String>(6001, "master", 6000);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        masterService.stop();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    @Test
    public void testIt()
    {
        try {
            ReplicatedLockConnectorProxy proxy = new ReplicatedLockConnectorProxy(6501);
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
    }
}