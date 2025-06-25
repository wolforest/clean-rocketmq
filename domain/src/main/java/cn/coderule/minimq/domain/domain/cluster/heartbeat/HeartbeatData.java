

/**
 * $Id: HeartbeatData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.domain.domain.cluster.heartbeat;

import com.alibaba.fastjson2.JSON;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class HeartbeatData implements Serializable {
    private String clientID;
    private Set<ProducerData> producerDataSet = new HashSet<>();
    private Set<ConsumerData> consumerDataSet = new HashSet<>();
    private int heartbeatFingerprint = 0;
    private boolean isWithoutSub = false;

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public Set<ProducerData> getProducerDataSet() {
        return producerDataSet;
    }

    public void setProducerDataSet(Set<ProducerData> producerDataSet) {
        this.producerDataSet = producerDataSet;
    }

    public Set<ConsumerData> getConsumerDataSet() {
        return consumerDataSet;
    }

    public void setConsumerDataSet(Set<ConsumerData> consumerDataSet) {
        this.consumerDataSet = consumerDataSet;
    }

    public int getHeartbeatFingerprint() {
        return heartbeatFingerprint;
    }

    public void setHeartbeatFingerprint(int heartbeatFingerprint) {
        this.heartbeatFingerprint = heartbeatFingerprint;
    }

    public boolean isWithoutSub() {
        return isWithoutSub;
    }

    public void setWithoutSub(boolean withoutSub) {
        isWithoutSub = withoutSub;
    }

    @Override
    public String toString() {
        return "HeartbeatData [clientID=" + clientID + ", producerDataSet=" + producerDataSet
            + ", consumerDataSet=" + consumerDataSet + "]";
    }

    public int computeHeartbeatFingerprint() {
        HeartbeatData heartbeatDataCopy = JSON.parseObject(JSON.toJSONString(this), HeartbeatData.class);
        for (ConsumerData consumerData : heartbeatDataCopy.getConsumerDataSet()) {
            for (SubscriptionData subscriptionData : consumerData.getSubscriptionDataSet()) {
                subscriptionData.setSubVersion(0L);
            }
        }
        heartbeatDataCopy.setWithoutSub(false);
        heartbeatDataCopy.setHeartbeatFingerprint(0);
        heartbeatDataCopy.setClientID("");
        return JSON.toJSONString(heartbeatDataCopy).hashCode();
    }
}
