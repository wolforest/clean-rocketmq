package cn.coderule.minimq.broker.infra.remote;

public abstract class AbstractRemoteStore {
    protected final RemoteLoadBalance loadBalance;

    public AbstractRemoteStore(RemoteLoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}
