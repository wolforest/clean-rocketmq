
package cn.coderule.minimq.rpc.registry.protocol.body;

import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import com.google.common.base.Objects;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BrokerMemberGroup extends RpcSerializable {
    private String cluster;
    private String brokerName;
    private Map<Long/* brokerId */, String/* broker address */> brokerAddrs;

    // Provide default constructor for serializer
    public BrokerMemberGroup() {
        this.brokerAddrs = new HashMap<>();
    }

    public BrokerMemberGroup(final String cluster, final String brokerName) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = new HashMap<>();
    }

    public long minimumBrokerId() {
        if (this.brokerAddrs.isEmpty()) {
            return 0;
        }
        return Collections.min(brokerAddrs.keySet());
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(final String cluster) {
        this.cluster = cluster;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(final String brokerName) {
        this.brokerName = brokerName;
    }

    public Map<Long, String> getBrokerAddrs() {
        return brokerAddrs;
    }

    public void setBrokerAddrs(final Map<Long, String> brokerAddrs) {
        this.brokerAddrs = brokerAddrs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BrokerMemberGroup that = (BrokerMemberGroup) o;
        return Objects.equal(cluster, that.cluster) &&
            Objects.equal(brokerName, that.brokerName) &&
            Objects.equal(brokerAddrs, that.brokerAddrs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cluster, brokerName, brokerAddrs);
    }

    @Override
    public String toString() {
        return "BrokerMemberGroup{" +
            "cluster='" + cluster + '\'' +
            ", brokerName='" + brokerName + '\'' +
            ", brokerAddrs=" + brokerAddrs +
            '}';
    }
}
