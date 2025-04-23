package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.Topic;
import java.util.concurrent.CompletableFuture;

public class RemoteTopicStore extends AbstractRemoteStore {
    public RemoteTopicStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    public CompletableFuture<Topic> getTopic(String topicName) {
        return null;
    }
}
