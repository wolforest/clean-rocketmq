package cn.coderule.minimq.domain.domain.cluster.selector;

import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRouteWrapper;
import com.google.common.base.MoreObjects;


public class MessageQueueView {
    public static final MessageQueueView WRAPPED_EMPTY_QUEUE = new MessageQueueView("", new RouteInfo(), null);

    private final MessageQueueSelector readSelector;
    private final MessageQueueSelector writeSelector;
    private final TopicRouteWrapper topicRouteWrapper;

    public MessageQueueView(String topic, RouteInfo topicRouteData, MQFaultStrategy mqFaultStrategy) {
        this.topicRouteWrapper = new TopicRouteWrapper(topicRouteData, topic);

        this.readSelector = new MessageQueueSelector(topicRouteWrapper, mqFaultStrategy, true);
        this.writeSelector = new MessageQueueSelector(topicRouteWrapper, mqFaultStrategy, false);
    }

    public RouteInfo getTopicRouteData() {
        return topicRouteWrapper.getTopicRouteData();
    }

    public TopicRouteWrapper getTopicRouteWrapper() {
        return topicRouteWrapper;
    }

    public String getTopicName() {
        return topicRouteWrapper.getTopicName();
    }

    public boolean isEmptyCachedQueue() {
        return this == WRAPPED_EMPTY_QUEUE;
    }

    public MessageQueueSelector getReadSelector() {
        return readSelector;
    }

    public MessageQueueSelector getWriteSelector() {
        return writeSelector;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("readSelector", readSelector)
            .add("writeSelector", writeSelector)
            .add("topicRouteWrapper", topicRouteWrapper)
            .toString();
    }
}
