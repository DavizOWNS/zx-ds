package dfs.management;

import dfs.paxos.INode;
import dfs.paxos.IValue;

import java.util.List;

/**
 * Created by Dávid on 14.1.2017.
 */
public class View implements IValue {
    private List<INode<View>> activeNodes;

    public View(List<INode<View>> activeNodes) {
        this.activeNodes = activeNodes;
    }

    public List<INode<View>> getActiveNodes()
    {
        return activeNodes;
    }
}
