package dfs.management;

import dfs.paxos.INode;
import dfs.paxos.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Manager implements IManager<View> {

    private String guid;
    private View currentView;
    private Node<View> mNode;

    public Manager(String guid)
    {
        this.guid = guid;
        mNode = new Node<>(guid, this);
        List<INode<View>> view = new ArrayList<>();
        view.add(mNode);
        currentView = new View(view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                doHearthbeats();
            }
        }).start();
    }

    @Override
    public void paxosCommit(int paxosInstance, View value)
    {
        currentView = value;
    }

    private void doHearthbeats()
    {
        while(true)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(isLowestId())
            {
                //send hearthbeat to others
                List<INode<View>> newView = new ArrayList<>();
                newView.add(mNode);
                for(INode<View> node : currentView.getActiveNodes())
                {
                    if(node == this) continue;
                    if(sendHearthbeat(node))
                        newView.add(node);
                }

                if(newView.size() != currentView.getActiveNodes().size())
                {
                    //TODO initiate paxos
                    mNode.run(0, newView, new View(newView));
                }
            }
            else
            {
                //send hearthbeat to lowest
                INode<View> lowestNode = getLowestNode();
                if(!sendHearthbeat(lowestNode))
                {
                    //TODO initiate paxos
                    List<INode<View>> newView = new ArrayList<>();
                    newView.add(mNode);
                    for(INode<View> node : currentView.getActiveNodes())
                    {
                        if(node == this) continue;
                        if(sendHearthbeat(node))
                            newView.add(node);
                    }

                    mNode.run(0, newView, new View(newView));
                }
            }
        }
    }

    private boolean sendHearthbeat(INode<View> node)
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Boolean> task = new Callable<Boolean>() {
            public Boolean call() {
                node.hearthbeat();
                return true;
            }
        };
        Future<Boolean> future = executor.submit(task);
        try {
            Boolean result = future.get(100, TimeUnit.MILLISECONDS);

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
        } finally {
            future.cancel(true); // may or may not desire this
        }
    }

    private boolean isLowestId()
    {
        boolean result = false;
        int lowestId = Integer.MAX_VALUE;
        for(INode<View> node : currentView.getActiveNodes())
        {
            if(node.getGuid().hashCode() < lowestId)
            {
                lowestId = node.getGuid().hashCode();
                if(node == mNode)
                    result = true;
                else
                    result = false;
            }
        }

        return result;
    }
    private INode<View> getLowestNode()
    {
        INode<View> result = null;
        int lowestId = Integer.MAX_VALUE;
        for(INode<View> node : currentView.getActiveNodes())
        {
            if(node.getGuid().hashCode() < lowestId)
            {
                lowestId = node.getGuid().hashCode();
                result = node;
            }
        }

        return result;
    }
}
