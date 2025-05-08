package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.TopicStore;
import java.util.concurrent.CompletableFuture;

public class RemoteTopicStore extends AbstractRemoteStore implements TopicStore {
    public RemoteTopicStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public CompletableFuture<Topic> getAsync(String topicName) {
        return null;
    }
}
