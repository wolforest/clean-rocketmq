

/*
  $Id: QueueData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.domain.domain.cluster.route;

import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class QueueInfo implements Comparable<QueueInfo> {
    // not compatible with rocketmq
    private MessageType messageType = null;

    private String brokerName;
    private int readQueueNums;
    private int writeQueueNums;
    private int perm;
    private int topicSysFlag;

    public QueueInfo() {

    }

    public static QueueInfo from(String groupName, Topic topic) {
        QueueInfo queueInfo = new QueueInfo();
        queueInfo.setBrokerName(groupName);

        queueInfo.setReadQueueNums(topic.getReadQueueNums());
        queueInfo.setWriteQueueNums(topic.getWriteQueueNums());
        queueInfo.setPerm(topic.getPerm());
        queueInfo.setTopicSysFlag(topic.getTopicSysFlag());
        queueInfo.setMessageType(topic.getMessageType());

        return queueInfo;
    }

    public Topic toTopic(String topicName) {
        Topic topic = new Topic();
        topic.setTopicName(topicName);
        topic.setReadQueueNums(readQueueNums);
        topic.setWriteQueueNums(writeQueueNums);
        topic.setPerm(perm);
        topic.setTopicSysFlag(topicSysFlag);
        topic.setMessageType(messageType);
        return topic;
    }

    // Deep copy QueueData
    public QueueInfo(QueueInfo queueInfo) {
        this.brokerName = queueInfo.brokerName;
        this.readQueueNums = queueInfo.readQueueNums;
        this.writeQueueNums = queueInfo.writeQueueNums;
        this.perm = queueInfo.perm;
        this.topicSysFlag = queueInfo.topicSysFlag;
        this.messageType = queueInfo.messageType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brokerName == null) ? 0 : brokerName.hashCode());
        result = prime * result + perm;
        result = prime * result + readQueueNums;
        result = prime * result + writeQueueNums;
        result = prime * result + topicSysFlag;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QueueInfo other = (QueueInfo) obj;
        if (brokerName == null) {
            if (other.brokerName != null)
                return false;
        } else if (!brokerName.equals(other.brokerName))
            return false;
        if (perm != other.perm)
            return false;
        if (readQueueNums != other.readQueueNums)
            return false;
        if (writeQueueNums != other.writeQueueNums)
            return false;
        return topicSysFlag == other.topicSysFlag;
    }

    @Override
    public String toString() {
        return "QueueData [brokerName=" + brokerName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=" + perm + ", topicSysFlag=" + topicSysFlag
            + "]";
    }

    @Override
    public int compareTo(QueueInfo o) {
        return this.brokerName.compareTo(o.getBrokerName());
    }

}
