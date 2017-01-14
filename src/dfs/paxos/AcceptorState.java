package dfs.paxos;

/**
 * Created by Dávid on 14.1.2017.
 */
public class AcceptorState<T extends IValue> {
    //n_h (highest prepare seen)
    public int highestPrepare = Integer.MIN_VALUE;
    //instance_h, (highest instance accepted)
    public int highestInstanceAccepted = Integer.MIN_VALUE;
    //n_a, v_a (highest accept seen)
    public int highestAccept = Integer.MIN_VALUE;
    public T highestAcceptedValue = null;

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
