package dfs.paxos;

import java.io.Serializable;

/**
 * Created by Dávid on 14.1.2017.
 */
public class AcceptorState<T extends IValue> implements Serializable{
    //n_h (highest prepare seen)
    private int highestPrepare = Integer.MIN_VALUE;
    //instance_h, (highest instance accepted)
    private int highestInstanceAccepted = Integer.MIN_VALUE;
    //n_a, v_a (highest accept seen)
    private int highestAccept = Integer.MIN_VALUE;
    private T highestAcceptedValue = null;

    public void setHighestPrepare(int highestPrepare) {
        this.highestPrepare = highestPrepare;
    }

    public void setHighestInstanceAccepted(int highestInstanceAccepted) {
        this.highestInstanceAccepted = highestInstanceAccepted;
    }

    public void setHighestAccept(int highestAccept) {
        this.highestAccept = highestAccept;
    }

    public void setHighestAcceptedValue(T highestAcceptedValue) {
        this.highestAcceptedValue = highestAcceptedValue;
    }

    public int getHighestPrepare() {
        return highestPrepare;
    }

    public int getHighestInstanceAccepted() {
        return highestInstanceAccepted;
    }

    public int getHighestAccept() {
        return highestAccept;
    }

    public T getHighestAcceptedValue() {
        return highestAcceptedValue;
    }

    public void load(String data)
    {
        String[] parts = data.split("\n");
        highestPrepare = Integer.parseInt(parts[0]);
        highestInstanceAccepted = Integer.parseInt(parts[1]);
        highestAccept = Integer.parseInt(parts[2]);
    }
    public String save()
    {
        return String.valueOf(highestPrepare) + "\n" +
                String.valueOf(highestInstanceAccepted) + "\n" +
                String.valueOf(highestAccept);
    }
}
