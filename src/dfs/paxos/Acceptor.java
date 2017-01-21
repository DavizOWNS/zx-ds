package dfs.paxos;

import dfs.replication.IState;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Acceptor<TVal extends IValue, TService extends IState> implements IAcceptor<TVal> {
    //must persist across reboots
    private AcceptorState<TVal> mState;
    private Map<Integer, TVal> oldInstances;
    private INode<TVal, TService> mNode;

    public Acceptor(INode<TVal, TService> node)
    {
        this.mNode = node;
        oldInstances = new HashMap<>();
        try {
            mState = loadState(node.getGuid());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    synchronized public PrepareResponse<TVal> prepare(int agreementInstance, int id)
    {
        System.out.println("ACCEPTOR: prepare instance: " + agreementInstance + "   id: " + id);
        //if instance <= instance_h
        if(agreementInstance <= mState.getHighestInstanceAccepted())
        {
            System.out.println("ACCEPTOR: oldInstance(" + agreementInstance + ") id: " + id);
            //reply oldinstance(instance, instance_value)
            return new PrepareResponse<TVal>(agreementInstance, oldInstances.get(agreementInstance), PrepareResponse.State.OLD_INSTANCE);
        }
        //else if n > n_h
        else if (id > mState.getHighestPrepare())
        {
            System.out.println("ACCEPTOR: id>prepare\tid: " + id + "  highest_prep: " + mState.getHighestPrepare());
            //n_h = n
            mState.setHighestPrepare(id);
            saveState();
            //reply prepare_ok(n_a, v_a)
            return new PrepareResponse<TVal>(mState.getHighestAccept(), mState.getHighestAcceptedValue(), PrepareResponse.State.OK);

        }

        System.out.println("ACCEPTOR: prepare failed  instance: " + agreementInstance + "   id: " + id);
        return null;
    }

    @Override
    synchronized public boolean accept(int agreementInstance, int id, TVal value)
    {
        //if n >= n_h
        if(id >= mState.getHighestPrepare())
        {
            System.out.println("ACCEPTOR: accept_ok   instance: " + agreementInstance + "   id: " + id);
            //n_a = n
            mState.setHighestAccept(id);
            //v_a = v
            mState.setHighestAcceptedValue(value);
            saveState();
            //reply accept_ok(n)
            return true;
        }

        System.out.println("ACCEPTOR: accept failed  instance: " + agreementInstance + "   id: " + id);
        return false;
    }

    @Override
    synchronized public void decide(int agreementInstance, TVal value)
    {
        //paxos_commit(instance, v)
        oldInstances.put(agreementInstance, value);
        if(agreementInstance > mState.getHighestInstanceAccepted())
            mState.setHighestInstanceAccepted(agreementInstance);
        saveState();

        try {
            mNode.getManager().paxosCommit(agreementInstance, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
