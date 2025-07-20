package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import cn.coderule.minimq.rpc.store.facade.TimerFacade;

public class TimerClient extends AbstractStoreClient implements StoreClient, TimerFacade {
    public TimerClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return null;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(long delayTime) {
        return null;
    }
}
