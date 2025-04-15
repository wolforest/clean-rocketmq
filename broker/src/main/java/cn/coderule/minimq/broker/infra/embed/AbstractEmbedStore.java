package cn.coderule.minimq.broker.infra.embed;

public abstract class AbstractEmbedStore {
    protected final EmbedLoadBalance loadBalance;

    public AbstractEmbedStore(EmbedLoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}
