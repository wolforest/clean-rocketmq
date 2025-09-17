package cn.coderule.minimq.broker.infra.embed;

public abstract class AbstractEmbedStore {
    protected final EmbedLoadBalance loadBalance;

    public AbstractEmbedStore(EmbedLoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public boolean containsTopic(String topicName) {
        return loadBalance.containsTopic(topicName);
    }

    public boolean containsSubscription(String groupName) {
        return loadBalance.containsSubscription(groupName);
    }

    public boolean isClusterGroup(String storeGroup) {
        return loadBalance.isEmbed(storeGroup);
    }
}
