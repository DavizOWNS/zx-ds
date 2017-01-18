package dfs.replication;

import dfs.management.IManager;
import dfs.management.Manager;
import dfs.management.View;
import dfs.util.IAction;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by DAVID on 16.1.2017.
 */
public class ReplicatedService<TService extends Remote> extends UnicastRemoteObject implements IReplicatedService<TService> {
    private static Registry registry;
    private TService service;
    private int mPort;
    private Manager<TService> mManager;

    public ReplicatedService(int port, TService service) throws RemoteException {
        super();
        this.service = service;
        this.mPort = port;

        mManager = new Manager<TService>(UUID.randomUUID().toString(), this);

        try {
            export(service.getClass().getName());
        }
        catch (RemoteException e)
        {
            mManager.dispose();
            throw e;
        }

        System.out.println("Starting node " + mManager.getId());
    }

    public ReplicatedService(int port, TService service, int existingReplicaPort) throws RemoteException
    {
        this(port, service);

        System.out.println("Looking for other replica at port " + existingReplicaPort);
        Registry existingRegistry = LocateRegistry.getRegistry(existingReplicaPort);
        try {
            IReplicatedService<TService> other = (IReplicatedService<TService>) existingRegistry.lookup(service.getClass().getName());
            System.out.println("Replica found");

            other.getManager().addNode(mManager.getNode());
        } catch (NotBoundException e) {
            System.out.println(e.getMessage());

            e.printStackTrace();
        }
    }

    private void export(String registryName) throws RemoteException
    {
        //IManager<View> stub = (IManager<View>) UnicastRemoteObject.exportObject(mManager, mPort);
        System.out.println("Starting " + registryName + " at port " + mPort);
        System.out.println("Retrieving registry");
        registry = LocateRegistry.createRegistry(mPort);

        System.out.println("Binding...");
        try {
            registry.bind(registryName, this);
        } catch (AlreadyBoundException e) {
            System.out.println("Already bound");
            e.printStackTrace();
        }

        System.out.println("Object " + registryName + " bound to registry at port " + mPort);
    }

    @Override
    public <TRes extends Serializable> TRes useService(IAction<TRes, TService> action) throws RemoteException {
        IManager<View<TService>, TService> master = mManager.getMasterNode().getManager();
        boolean isMaster = Objects.equals(mManager.getId(), master.getId());
        TRes result = null;
        try {
            //TODO
            throw new RemoteException();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public IManager<View<TService>, TService> getManager() {
        return mManager;
    }

    public void stop()
    {
        if(registry == null)
            return;

        System.out.println("Stopping...");
        try {
            registry.unbind(service.getClass().getName());

            registry = null;
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            if(!UnicastRemoteObject.unexportObject(mManager, true))
                System.out.println("Could not unexport object");
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }

        mManager.dispose();
    }
}
