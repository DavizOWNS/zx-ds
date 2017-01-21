package dfs.replication;

import java.io.Serializable;

/**
 * Created by DAVID on 20.1.2017.
 */
public class Viewstamp implements Serializable{
    private int viewID;
    private int sequenceNumber;

    public Viewstamp(int viewID, int sequenceNumber) {
        this.viewID = viewID;
        this.sequenceNumber = sequenceNumber;
    }

    public Viewstamp IncrementSeq()
    {
        return new Viewstamp(viewID, sequenceNumber+1);
    }

    public int getViewID() {
        return viewID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
