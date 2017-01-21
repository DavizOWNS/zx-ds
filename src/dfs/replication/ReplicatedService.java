package dfs.replication;

import dfs.management.IManager;
import dfs.management.Manager;
import dfs.management.View;
import dfs.paxos.INode;
import dfs.util.IAction;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by DAVID on 16.1.2017.
 */
public class ReplicatedService<TService extends IState> extends UnicastRemoteObject implements IReplicatedService<TService> {
    private static final String REGISTRY_NAME = "replica";

    private static Registry registry;
    private TService service;
    private int mPort;
    private Manager<TService> mManager;
    private boolean isInViewchange;
    private ReplicaState state;

    public ReplicatedService(int port, TService service) throws RemoteException {
        super();
        this.service = service;
        this.mPort = port;

        mManager = new Manager<TService>(UUID.randomUUID().toString(), this);

        try {
            export(REGISTRY_NAME);
        }
        catch (RemoteException e)
        {
            mManager.dispose();
            throw e;
        }

        state = new ReplicaState();
        state.nextSeqNo = Integer.MIN_VALUE;
        state.viewID = mManager.getViewID();
        isInViewchange = false;

        System.out.println("Starting node " + mManager.getId());
    }

    public ReplicatedService(int port, TService service, int existingReplicaPort) throws RemoteException
    {
        this(port, service);

        isInViewchange = true;
        System.out.println("Looking for other replica at port " + existingReplicaPort);
        Registry existingRegistry = LocateRegistry.getRegistry(existingReplicaPort);
        try {
            IReplicatedService<TService> other = (IReplicatedService<TService>) existingRegistry.lookup(REGISTRY_NAME);
            System.out.println("Replica found");

            other.getManager().getMaster().addNode(mManager.getNode());

            isInViewchange = false;
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
    public <TRes extends Serializable> ServiceActionResult<TRes> useService(IAction<TRes, TService> action) throws RemoteException {
        IManager<View<TService>, TService> master = mManager.getMasterNode().getManager();
        boolean isMaster = Objects.equals(mManager.getId(), master.getId());
        if(!isMaster)
            return new ServiceActionResult<TRes>(null, ServiceActionResult.State.NOTPRIMARY);

        Viewstamp viewStamp = new Viewstamp(mManager.getViewID(), state.nextSeqNo++);
        List<INode<View<TService>, TService>> nodes = mManager.getCurrentView().getActiveNodes();
        List<INode<View<TService>, TService>> failedNodes = new ArrayList<>();
        for (INode<View<TService>, TService> node : nodes) {
            if (node.getGuid().equals(mManager.getId()))
                continue;

            try {
                TRes result = node.getManager().getService().useService(action, viewStamp);
            }
            catch (RemoteException ex)
            {
                failedNodes.add(node);
            }
        }

        if(failedNodes.size() > 0)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (INode<View<TService>, TService> node : failedNodes) {
                        mManager.removeNode(node);
                    }
                }
            }).start();
        }

        return new ServiceActionResult<TRes>(useServiceInternal(action), ServiceActionResult.State.OK);
    }

    private <TRes extends Serializable> TRes useServiceInternal(IAction<TRes, TService> action) throws RemoteException {
        return action.execute(service);
    }

    @Override
    public <TRes extends Serializable> TRes useService(IAction<TRes, TService> action, Viewstamp viewstampe) throws RemoteException {
        while(isInViewchange);

        if(viewstampe.getSequenceNumber() != state.nextSeqNo)
        {
            throw new RemoteException("BAD_STATE");
        }
        state.nextSeqNo++;

        return useServiceInternal(action);
    }

    @Override
    public IManager<View<TService>, TService> getManager() {
        return mManager;
    }

    public IReplicatedService<TService> getMaster() throws RemoteException {
        return mManager.getMasterNode().getManager().getService();
    }

    public void commitChange(View<TService> newView, int viewID)
    {
        try {
            if(mManager.isMaster())
            {
                state.viewID = viewID;
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        isInViewchange = true;
        new Thread(this::recovery).start();
    }

    public ReplicaState getState() throws RemoteException
    {
        return state;
    }

    @Override
    public List<IReplicatedService<TService>> members() throws RemoteException {
        List<IReplicatedService<TService>> result = new ArrayList<>();

        List<INode<View<TService>, TService>> nodes = mManager.getCurrentView().getActiveNodes();
        for (INode<View<TService>, TService> node : nodes) {
            result.add(node.getManager().getService());
        }

        return result;
    }

    private void recovery() {
        try {
            state = mManager.getMaster().getService().getState();
            setServiceState(mManager.getMaster().getService().getServiceState());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        isInViewchange = false;
    }

    public Object getServiceState() throws RemoteException
    {
        return service.getState();
    }
    public void setServiceState(Object state) throws RemoteException
    {
        service.setState(state);
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
