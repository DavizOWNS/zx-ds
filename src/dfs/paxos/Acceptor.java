package dfs.paxos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Acceptor<TVal extends IValue> implements IAcceptor<TVal> {
    //must persist across reboots
    private AcceptorState<TVal> mState;
    private Map<Integer, TVal> oldInstances;
    private INode mNode;

    public Acceptor(INode node)
    {
        this.mNode = node;
        oldInstances = new HashMap<>();
        mState = loadState(node.getGuid());
    }

    @Override
    synchronized public PrepareResponse<TVal> prepare(int agreementInstance, int id)
    {
        //if instance <= instance_h
        if(agreementInstance <= mState.highestInstanceAccepted)
        {
            //reply oldinstance(instance, instance_value)
            return new PrepareResponse<TVal>(agreementInstance, oldInstances.get(agreementInstance), PrepareResponse.State.OLD_INSTANCE);
        }
        //else if n > n_h
        else if (id > mState.highestPrepare)
        {
            //n_h = n
            mState.highestPrepare = id;
            saveState();
            //reply prepare_ok(n_a, v_a)
            return new PrepareResponse<TVal>(mState.highestAccept, mState.highestAcceptedValue, PrepareResponse.State.OK);

        }

        return null;
    }

    @Override
    synchronized public boolean accept(int agreementInstance, int id, TVal value)
    {
        //if n >= n_h
        if(id >= mState.highestPrepare)
        {
            //n_a = n
            mState.highestAccept = id;
            //v_a = v
            mState.highestAcceptedValue = value;
            saveState();
            //reply accept_ok(n)
            return true;
        }

        return false;
    }

    @Override
    synchronized public void decide(int agreementInstance, TVal value)
    {
        //paxos_commit(instance, v)
        oldInstances.put(agreementInstance, value);
        if(agreementInstance > mState.highestInstanceAccepted)
            mState.highestInstanceAccepted = agreementInstance;
        saveState();
    }

    private void saveState()
    {
        //TODO
    }
    private AcceptorState<TVal> loadState(String id)
    {
        //TODO
        return new AcceptorState<>();
    }
}
