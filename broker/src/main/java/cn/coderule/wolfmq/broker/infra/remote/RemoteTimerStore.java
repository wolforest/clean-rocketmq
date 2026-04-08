package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.client.TimerClient;
import cn.coderule.wolfmq.rpc.store.facade.TimerFacade;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteTimerStore extends AbstractRemoteStore implements TimerFacade {
    private final BrokerConfig brokerConfig;
    private final ConcurrentMap<String, TimerClient> clientMap;
    private final RpcClient rpcClient;

    public RemoteTimerStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        clientMap = new ConcurrentHashMap<>();
        this.rpcClient = rpcClient;
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
