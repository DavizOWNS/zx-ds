package dfs.replication;

import java.io.Serializable;

/**
 * Created by DAVID on 20.1.2017.
 */
public class ReplicaState implements Serializable {
    public int viewID;
    public int nextSeqNo;
}
