package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.meta.TopicFacade;
import java.util.concurrent.CompletableFuture;

public class RemoteTopicStore extends AbstractRemoteStore implements TopicFacade {
    public RemoteTopicStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return null;
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }
}
