package dfs.lockservice;

import java.io.Serializable;

/**
 * Created by DAVID on 23.10.2016.
 */
public class Lock implements Serializable{
    private final String lockId;
    private final String ownerId;
    private final long sequenceId;

    public Lock(String lockId, String ownerId, long sequenceId)
    {
        this.lockId = lockId;
        this.ownerId = ownerId;
        this.sequenceId = sequenceId;
    }

    public String getLockId() {return lockId;}
    public String getOwnerId() {return ownerId;}
    public long getSequenceId() {return sequenceId;}

    /*@Override
    public boolean equals(Object obj) {
        if(obj instanceof Lock)
        {
            Lock other = (Lock)obj;

            return lockId.equals(other.lockId) && ownerId.equals(other.ownerId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return lockId.hashCode() ^ ownerId.hashCode();
    }*/
}
