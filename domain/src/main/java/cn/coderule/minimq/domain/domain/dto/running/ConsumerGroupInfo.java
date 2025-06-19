package cn.coderule.minimq.domain.domain.dto.running;

import cn.coderule.minimq.domain.domain.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.domain.enums.message.MessageModel;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ConsumerGroupInfo {
    private final String groupName;
    private final ConcurrentMap<String/* Topic */, SubscriptionData> subscriptionTable = new ConcurrentHashMap<>();
    private final ConcurrentMap<Channel, ClientChannelInfo> channelInfoTable = new ConcurrentHashMap<>(16);
    private volatile ConsumeType consumeType;
    private volatile MessageModel messageModel;
    private volatile ConsumeStrategy consumeFromWhere;
    private volatile long lastUpdateTimestamp = System.currentTimeMillis();

    public ConsumerGroupInfo(String groupName, ConsumeType consumeType, MessageModel messageModel,
        ConsumeStrategy consumeFromWhere) {
        this.groupName = groupName;
        this.consumeType = consumeType;
        this.messageModel = messageModel;
        this.consumeFromWhere = consumeFromWhere;
    }

    public ConsumerGroupInfo(String groupName) {
        this.groupName = groupName;
    }

    public boolean existsChannel(String clientId) {
        return null != findChannel(clientId);
    }

    public ClientChannelInfo findChannel(String clientId) {
        for (Entry<Channel, ClientChannelInfo> next : this.channelInfoTable.entrySet()) {
            if (next.getValue().getClientId().equals(clientId)) {
                return next.getValue();
            }
        }

        return null;
    }

    public ConcurrentMap<String, SubscriptionData> getSubscriptionTable() {
        return subscriptionTable;
    }

    public ClientChannelInfo findChannel(final Channel channel) {
        return this.channelInfoTable.get(channel);
    }

    public ConcurrentMap<Channel, ClientChannelInfo> getChannelInfoTable() {
        return channelInfoTable;
    }

    public List<Channel> getAllChannel() {
        return new ArrayList<>(this.channelInfoTable.keySet());
    }

    public List<String> getAllClientId() {
        List<String> result = new ArrayList<>();

        for (Entry<Channel, ClientChannelInfo> entry : this.channelInfoTable.entrySet()) {
            ClientChannelInfo clientChannelInfo = entry.getValue();
            result.add(clientChannelInfo.getClientId());
        }

        return result;
    }

    public boolean unregisterChannel(final ClientChannelInfo clientChannelInfo) {
        ClientChannelInfo old = this.channelInfoTable.remove(clientChannelInfo.getChannel());
        if (old != null) {
            log.info("unregister a consumer[{}] from consumerGroupInfo {}", this.groupName, old.toString());
            return true;
        }
        return false;
    }

    public ClientChannelInfo doChannelCloseEvent(final String remoteAddr, final Channel channel) {
        final ClientChannelInfo info = this.channelInfoTable.remove(channel);
        if (info != null) {
            log.warn("NETTY EVENT: remove idle channel[{}] from ConsumerGroupInfo, consumerGroup: {}",
                info, groupName);
        }

        return info;
    }

    /**
     * Update {@link #channelInfoTable} in {@link ConsumerGroupInfo}
     *
     * @param infoNew Channel info of new client.
     * @param consumeType consume type of new client.
     * @param model message consuming model (CLUSTERING/BROADCASTING) of new client.
     * @param strategy indicate the position when the client consume message firstly.
     * @return the result that if new connector is connected or not.
     */
    public boolean updateChannel(ClientChannelInfo infoNew, ConsumeType consumeType,
        MessageModel model, ConsumeStrategy strategy) {
        this.consumeType = consumeType;
        this.messageModel = model;
        this.consumeFromWhere = strategy;

        this.lastUpdateTimestamp = System.currentTimeMillis();
        ClientChannelInfo infoOld = this.channelInfoTable.get(infoNew.getChannel());
        if (null != infoOld && !infoOld.getClientId().equals(infoNew.getClientId())) {
            return updateConflictChannel(infoOld, infoNew);
        }

        return this.updateChannel(infoNew);
    }

    private boolean updateConflictChannel(ClientChannelInfo infoOld, ClientChannelInfo infoNew) {
        log.error("ConsumerGroupInfo: clientId conflict: group={}, old clientChannelInfo={}, new clientChannelInfo={}",
            groupName, infoOld, infoNew);

        this.channelInfoTable.put(infoNew.getChannel(), infoNew);
        infoOld.setLastUpdateTime(this.lastUpdateTimestamp);

        return false;
    }

    private boolean updateChannel(ClientChannelInfo infoNew) {
        ClientChannelInfo prev = this.channelInfoTable.put(infoNew.getChannel(), infoNew);
        if (null == prev) {
            log.info("new consumer connected, group: {} {} {} channel: {}",
                this.groupName, consumeType, this.messageModel, infoNew);
        }

        infoNew.setLastUpdateTime(this.lastUpdateTimestamp);

        return prev == null;
    }

    /**
     * Update subscription.
     *
     * @param subList set of {@link SubscriptionData}
     * @return the boolean indicates the subscription has changed or not.
     */
    public boolean updateSubscription(final Set<SubscriptionData> subList) {
        boolean updated = false;
        Set<String> topicSet = new HashSet<>();
        for (SubscriptionData sub : subList) {
            SubscriptionData old = this.subscriptionTable.get(sub.getTopic());
            if (old == null) {
                SubscriptionData prev = this.subscriptionTable.putIfAbsent(sub.getTopic(), sub);
                if (null == prev) {
                    updated = true;
                    log.info("subscription changed, add new topic, group: {} {}",
                        this.groupName, sub);
                }
            } else if (sub.getSubVersion() > old.getSubVersion()) {
                if (this.consumeType == ConsumeType.CONSUME_PASSIVELY) {
                    log.info("subscription changed, group: {} OLD: {} NEW: {}",
                        this.groupName, old, sub);
                }

                this.subscriptionTable.put(sub.getTopic(), sub);
            }
            // Add all new topics to the HashSet
            topicSet.add(sub.getTopic());
        }

        Iterator<Entry<String, SubscriptionData>> it = this.subscriptionTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, SubscriptionData> next = it.next();
            String oldTopic = next.getKey();
            // Check HashSet with O(1) time complexity
            if (topicSet.contains(oldTopic)) {
                continue;
            }

            log.warn("subscription changed, group: {} remove topic {} {}", this.groupName, oldTopic, next.getValue().toString());

            it.remove();
            updated = true;
        }

        this.lastUpdateTimestamp = System.currentTimeMillis();

        return updated;
    }

    public Set<String> getSubscribeTopics() {
        return subscriptionTable.keySet();
    }

    public SubscriptionData findSubscriptionData(final String topic) {
        return this.subscriptionTable.get(topic);
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

    public String getGroupName() {
        return groupName;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public ConsumeStrategy getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeStrategy consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }
}
