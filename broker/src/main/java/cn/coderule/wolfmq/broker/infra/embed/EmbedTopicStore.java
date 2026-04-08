package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import java.util.concurrent.CompletableFuture;

public class EmbedTopicStore extends AbstractEmbedStore implements TopicFacade {
    private final TopicStore topicStore;

    public EmbedTopicStore(TopicStore topicStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.topicStore = topicStore;
    }

    public boolean exists(String topicName) {
        return topicStore.exists(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return topicStore.getTopic(topicName);
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return CompletableFuture.completedFuture(
            topicStore.getTopic(topicName)
        );
    }


    @Override
    public void saveTopic(TopicRequest request) {
        topicStore.saveTopic(request);
    }

    @Override
    public void deleteTopic(TopicRequest request) {
        topicStore.deleteTopic(request);
    }

}
