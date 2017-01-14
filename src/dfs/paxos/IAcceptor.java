package dfs.paxos;

/**
 * Created by Dávid on 14.1.2017.
 */
public interface IAcceptor<T extends IValue> {
    PrepareResponse<T> prepare(int agreementInstance, int id);

    boolean accept(int agreementInstance, int id, T value);

    void decide(int agreementInstance, T value);
}
