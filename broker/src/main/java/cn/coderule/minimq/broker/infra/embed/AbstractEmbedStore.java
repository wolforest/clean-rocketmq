package cn.coderule.minimq.broker.infra.embed;

public class AbstractEmbedStore {
    protected final EmbedLoadBalance loadBalance;

    public AbstractEmbedStore(EmbedLoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}
