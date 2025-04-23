package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.Topic;

public class RemoteTopicStore extends AbstractRemoteStore {
    public RemoteTopicStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    public Topic getTopic(String topicName) {
        return null;
    }
}
