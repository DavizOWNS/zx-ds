package dfs.paxos;

import java.io.Serializable;

/**
 * Created by Dávid on 14.1.2017.
 */
public class PrepareResponse<T extends IValue> implements Serializable{
    public enum State
    {
        OK,
        OLD_INSTANCE
    }
    private int id;
    private T value;
    private State state;

    public PrepareResponse(int id, T value, State state) {
        this.id = id;
        this.value = value;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

    public State getState() {
        return state;
    }
}
