package dfs.management;

import dfs.paxos.INode;
import dfs.paxos.IValue;
import dfs.replication.IState;

import java.rmi.Remote;
import java.util.List;

/**
 * Created by Dávid on 14.1.2017.
 */
public class View<TService extends IState> implements IValue {
    private List<INode<View<TService>, TService>> activeNodes;

    public View(List<INode<View<TService>, TService>> activeNodes) {
        this.activeNodes = activeNodes;
    }

    public List<INode<View<TService>, TService>> getActiveNodes()
    {
        return activeNodes;
    }
}
