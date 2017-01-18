package dfs.replication;

import dfs.lockservice.LockConnector;
import dfs.lockservice.LockServer;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Created by DAVID on 16.1.2017.
 */
public class ReplicationModuleMain {

    public static void main(String[] args) throws IOException {
        boolean startAsMaster = args.length == 1;
        LockServer lockServer = new LockServer();
        ReplicatedService<LockConnector> service = null;
        try {
            if (startAsMaster) {
                service = new ReplicatedService<LockConnector>(Integer.valueOf(args[0]), new LockServer());
            } else {
                service = new ReplicatedService<LockConnector>(Integer.valueOf(args[0]), new LockServer(), Integer.valueOf(args[1]));
            }

            System.in.read();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            if(service != null)
                service.stop();
        }
    }
}
