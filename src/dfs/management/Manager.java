package dfs.management;

import dfs.paxos.INode;
import dfs.paxos.Node;
import dfs.replication.IReplicatedService;
import dfs.util.SerializableObject;
import jdk.nashorn.internal.codegen.CompilerConstants;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Manager<TService extends  Remote> extends UnicastRemoteObject implements Serializable, IManager<View<TService>, TService> {

    private String guid;
    private View<TService> currentView;
    private Node<View<TService>, TService> mNode;
    private int nextPaxosInstance = 0;
    private final SerializableObject viewLock;
    private boolean isRunning;
    private IReplicatedService<TService> service;

    public Manager(String guid, IReplicatedService<TService> service) throws RemoteException {
        super();
        this.viewLock = new SerializableObject();
        this.guid = guid;
        this.service = service;
        mNode = new Node<>(guid, this);
        List<INode<View<TService>, TService>> view = new ArrayList<>();
        view.add(mNode);
        currentView = new View<TService>(view);

        isRunning = true;
        new Thread(this::doHearthbeats).start();
    }

    @Override
    public void paxosCommit(int paxosInstance, View<TService> value)
    {
        currentView = value;
        nextPaxosInstance = paxosInstance+1;

        System.out.println("["+ guid +"]Paxos successfull. instance: " + paxosInstance + "   nodes:");
        for (int i = 0; i < value.getActiveNodes().size(); i++)
        {
            try {
                System.out.println("\t" + value.getActiveNodes().get(i).getGuid());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IReplicatedService<TService> getService() throws RemoteException {
        return service;
    }

    public void addNode(INode<View<TService>, TService> node)
    {
        List<INode<View<TService>, TService>> newView = null;
        synchronized (viewLock)
        {
            newView = new ArrayList<>(currentView.getActiveNodes());
            newView.add(node);
        }
        System.out.println("[" + getId() + "]Starting paxos: " + (nextPaxosInstance));
        try {
            mNode.run(nextPaxosInstance++, newView, new View<TService>(newView));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void doHearthbeats()
    {
        while(isRunning)
        {
            doHearthbeat();
        }
    }
    public void doHearthbeat()
    {
        System.out.println("["+ getId() + "] doing hearthbeat... [" + currentView.getActiveNodes().size() + "]");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean isLowestId = false;
        try {
            isLowestId = isLowestId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(isLowestId)
        {
            //send hearthbeat to others
            List<INode<View<TService>, TService>> newView = new ArrayList<>();
            newView.add(mNode);
            synchronized (viewLock)
            {
                for(INode<View<TService>, TService> node : currentView.getActiveNodes())
                {
                    try {
                        if(Objects.equals(node.getGuid(), mNode.getGuid())) continue;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    boolean isAlive = sendHearthbeat(node);
                    try {
                        System.out.println("[" + getId() + "] " + node.getGuid() + " alive: " + isAlive);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if(isAlive)
                        newView.add(node);
                }

                if(newView.size() != currentView.getActiveNodes().size())
                {
                    //TODO initiate paxos
                    try {
                        mNode.run(nextPaxosInstance++, newView, new View(newView));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            //send hearthbeat to lowest
            INode<View<TService>, TService> lowestNode = null;
            try {
                lowestNode = getLowestNode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            boolean isAlive = sendHearthbeat(lowestNode);
            try {
                System.out.println("[" + getId() + "] " + lowestNode.getGuid() + " alive: " + isAlive);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(!isAlive)
            {
                //TODO initiate paxos
                synchronized (viewLock)
                {
                    List<INode<View<TService>, TService>> newView = new ArrayList<>(currentView.getActiveNodes());
                    newView.remove(lowestNode);

                    try {
                        mNode.run(nextPaxosInstance++, newView, new View<TService>(newView));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean sendHearthbeat(INode<View<TService>, TService> node)
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Void> task = () -> {
            node.hearthbeat(mNode.getGuid());
            return null;
        };
        Future<Void> future = executor.submit(task);
        try {
            future.get(100, TimeUnit.MILLISECONDS);

            return true;
        } catch (TimeoutException ex) {
            // handle the timeout
            return false;
        } catch (InterruptedException e) {
            // handle the interrupts
            return false;
        } catch (ExecutionException e) {
            // handle other exceptions
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return  false;
        } finally {
            future.cancel(true); // may or may not desire this
        }
    }

    public void dispose()
    {
        isRunning = false;
    }

    public View getCurrentView() {
        return currentView;
    }

    private boolean isLowestId() throws RemoteException
    {
        return Objects.equals(mNode.getGuid(), currentView.getActiveNodes().get(0).getGuid());
    }

    public String getId() {
        return guid;
    }

    public INode<View<TService>, TService> getNode() {
        return mNode;
    }

    private INode<View<TService>, TService> getLowestNode() throws RemoteException {
        return currentView.getActiveNodes().get(0);
    }

    public INode<View<TService>, TService> getMasterNode()
    {
        return currentView.getActiveNodes().get(0);
    }
}
