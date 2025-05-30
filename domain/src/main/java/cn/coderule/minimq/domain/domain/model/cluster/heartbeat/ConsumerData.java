

/**
 * $Id: ConsumerData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.domain.domain.model.cluster.heartbeat;

import cn.coderule.minimq.domain.domain.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.domain.enums.message.MessageModel;
import java.util.HashSet;
import java.util.Set;

public class ConsumerData {
    private String groupName;
    private ConsumeType consumeType;
    private MessageModel messageModel;
    private ConsumeStrategy consumeFromWhere;
    private Set<SubscriptionData> subscriptionDataSet = new HashSet<>();
    private boolean unitMode;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ConsumeType getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(ConsumeType consumeType) {
        this.consumeType = consumeType;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public ConsumeStrategy getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeStrategy consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public Set<SubscriptionData> getSubscriptionDataSet() {
        return subscriptionDataSet;
    }

    public void setSubscriptionDataSet(Set<SubscriptionData> subscriptionDataSet) {
        this.subscriptionDataSet = subscriptionDataSet;
    }

    public boolean isUnitMode() {
        return unitMode;
    }

    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    @Override
    public String toString() {
        return "ConsumerData [groupName=" + groupName + ", consumeType=" + consumeType + ", messageModel="
            + messageModel + ", consumeFromWhere=" + consumeFromWhere + ", unitMode=" + unitMode
            + ", subscriptionDataSet=" + subscriptionDataSet + "]";
    }
}
