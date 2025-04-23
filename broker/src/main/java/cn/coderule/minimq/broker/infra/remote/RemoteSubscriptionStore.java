package cn.coderule.minimq.broker.infra.remote;

public class RemoteSubscriptionStore extends AbstractRemoteStore {
    public RemoteSubscriptionStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }
}
