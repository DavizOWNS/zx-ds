package dfs.replication;

import dfs.lockservice.ILockConnectorWithState;
import dfs.lockservice.LockConnector;
import dfs.lockservice.LockServer;
import dfs.util.IAction;
import dfs.util.SerializableObject;
import jdk.management.resource.internal.TotalResourceContext;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Created by DAVID on 16.1.2017.
 */
public class ReplicationModuleMain {

    public static void main(String[] args) throws IOException {
        boolean startAsMaster = args.length == 1;
        LockServer lockServer = new LockServer();
        ReplicatedService<ILockConnectorWithState> service = null;
        try {
            if (startAsMaster) {
                service = new ReplicatedService<ILockConnectorWithState>(Integer.valueOf(args[0]), lockServer);

            } else {
                service = new ReplicatedService<ILockConnectorWithState>(Integer.valueOf(args[0]), lockServer, Integer.valueOf(args[1]));
            }


            /*final String id = service.getManager().getId();
            service.getMaster().useService(new IAction<SerializableObject, LockConnector>() {
                @Override
                public SerializableObject execute(LockConnector obj) throws RemoteException {
                    obj.acquire("home", id, 0);
                    return null;
                }
            });*/
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            System.in.read();

            if(service != null)
                service.stop();
        }
    }
}
