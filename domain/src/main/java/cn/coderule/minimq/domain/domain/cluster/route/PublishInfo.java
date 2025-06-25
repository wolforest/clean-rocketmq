package cn.coderule.minimq.domain.domain.cluster.route;

import cn.coderule.common.lang.concurrent.thread.local.ThreadLocalSequence;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.service.common.QueueFilter;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PublishInfo implements Serializable {
    private boolean ordered = false;
    private boolean hasRoute = false;

    private RouteInfo routeInfo;
    private List<MessageQueue> queueList;

    private volatile ThreadLocalSequence sequence;

    public PublishInfo() {
        this.queueList = new ArrayList<>();
        this.sequence = new ThreadLocalSequence();
    }

    public void resetSequence() {
        this.sequence.reset();
    }

    public boolean isOk() {
        return CollectionUtil.notEmpty(queueList);
    }

    public MessageQueue selectOneMessageQueue(QueueFilter...filter) {
        return selectOneMessageQueue(this.queueList, this.sequence, filter);
    }

    private MessageQueue selectOneMessageQueue(List<MessageQueue> messageQueueList, ThreadLocalSequence sendQueue, QueueFilter ...filter) {
        if (messageQueueList == null || messageQueueList.isEmpty()) {
            return null;
        }

        if (filter != null && filter.length != 0) {
            for (int i = 0; i < messageQueueList.size(); i++) {
                int index = Math.abs(sendQueue.incrementAndGet() % messageQueueList.size());
                MessageQueue mq = messageQueueList.get(index);
                boolean filterResult = true;
                for (QueueFilter f: filter) {
                    Preconditions.checkNotNull(f);
                    filterResult &= f.filter(mq);
                }
                if (filterResult) {
                    return mq;
                }
            }

            return null;
        }

        int index = Math.abs(sendQueue.incrementAndGet() % messageQueueList.size());
        return messageQueueList.get(index);
    }


    public MessageQueue selectOneMessageQueue(final String lastBrokerName) {
        if (lastBrokerName == null) {
            return selectOneMessageQueue();
        }

        for (int i = 0; i < this.queueList.size(); i++) {
            int index = this.sequence.incrementAndGet();
            int pos = index % this.queueList.size();
            MessageQueue mq = this.queueList.get(pos);

            if (!mq.getGroupName().equals(lastBrokerName)) {
                return mq;
            }
        }
        return selectOneMessageQueue();
    }

    public MessageQueue selectOneMessageQueue() {
        int index = this.sequence.incrementAndGet();
        int pos = index % this.queueList.size();

        return this.queueList.get(pos);
    }

    public int getWriteQueueNumsByBroker(final String brokerName) {
        for (int i = 0; i < routeInfo.getQueueDatas().size(); i++) {
            final QueueInfo queueData = this.routeInfo.getQueueDatas().get(i);
            if (queueData.getBrokerName().equals(brokerName)) {
                return queueData.getWriteQueueNums();
            }
        }

        return -1;
    }


}
