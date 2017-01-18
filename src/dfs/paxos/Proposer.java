package dfs.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dávid on 14.1.2017.
 */
public class Proposer<TVal extends IValue, TService extends Remote> implements IProposer<TVal, TService> {
    private int highestId = 0;
    private AtomicInteger numInstancesRunning;
    private INode mNode;

    public Proposer(INode node)
    {
        mNode = node;
        numInstancesRunning = new AtomicInteger(0);
    }

    @Override
    public void run(int agreementInstance, List<INode<TVal, TService>> nodes, TVal value) throws RemoteException {
        numInstancesRunning.incrementAndGet();
        //choose n, unique and higher than any n seen so far
        int id = highestId + 1;
        //send prepare(instance, n) to all servers including self

        int highestPrepareID = id;
        TVal highestValue = value;
        int majority = nodes.size() / 2 + 1;
        int numReqOkResp = majority;
        for(IAcceptor<TVal> acc : nodes)
        {
            PrepareResponse<TVal> resp = acc.prepare(agreementInstance, id);
            if(resp == null)
                continue;
            if(resp.getState() == PrepareResponse.State.OK)
            {
                numReqOkResp--;
                if(resp.getId() > highestPrepareID)
                {
                    highestPrepareID = resp.getId();
                    highestValue = resp.getValue();
                }
            }
            else if(resp.getState() == PrepareResponse.State.OLD_INSTANCE) //already decided on value
            {

            }
        }
        if(highestPrepareID > highestId)
            highestId = highestPrepareID;
        //if prepare_ok(n_a, v_a) from majority:
        TVal newValue = null;
        if(numReqOkResp <= 0) {
            // /v' = v_a with highest n_a;
            newValue = highestValue;

            //send accept(instance, n, v') to all
            numReqOkResp = majority;
            for(IAcceptor<TVal> acc : nodes)
            {
                boolean isOK = acc.accept(agreementInstance, id, newValue);
                if(isOK)
                {
                    numReqOkResp--;
                }
            }
            //if accept_ok(n) from majority:
            if(numReqOkResp <= 0)
            {
                //send decided(instance, v') to all
                for(IAcceptor<TVal> acc : nodes)
                {
                    acc.decide(agreementInstance, newValue);
                }
            }
        }

        numInstancesRunning.decrementAndGet();
    }

    private boolean isStable()
    {
        return numInstancesRunning.get() == 0;
    }
}
