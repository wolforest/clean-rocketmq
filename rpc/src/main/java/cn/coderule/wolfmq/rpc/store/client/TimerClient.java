package cn.coderule.wolfmq.rpc.store.client;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.StoreClient;
import cn.coderule.wolfmq.rpc.store.facade.TimerFacade;

public class TimerClient extends AbstractStoreClient implements StoreClient, TimerFacade {
    public TimerClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint(RequestContext context) {
        return null;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(RequestContext context, long delayTime) {
        return null;
    }
}
