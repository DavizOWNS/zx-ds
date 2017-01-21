package dfs.replication;

import java.io.Serializable;

/**
 * Created by DAVID on 21.1.2017.
 */
public class ServiceActionResult<TRes> implements Serializable{
    public enum State {
        OK,
        NOTPRIMARY
    }
    private TRes result;
    private State state;

    public ServiceActionResult(TRes result, State state) {
        this.result = result;
        this.state = state;
    }

    public TRes getResult() {
        return result;
    }

    public State getState() {
        return state;
    }
}
