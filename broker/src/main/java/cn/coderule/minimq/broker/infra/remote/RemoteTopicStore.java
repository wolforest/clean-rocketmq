package cn.coderule.minimq.broker.infra.remote;

public class RemoteTopicStore extends AbstractRemoteStore {
    public RemoteTopicStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }
}
