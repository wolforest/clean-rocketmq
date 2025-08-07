package cn.coderule.minimq.domain.domain.cluster.cluster;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class GroupInfo implements Comparable<GroupInfo> {
    private String cluster;

    /**
     * for compatibility with rocketmq remoting protocol
     */
    private String brokerName;

    /**
     * The container that store the all single instances for the current broker replication cluster.
     * The key is the brokerId, and the value is the address of the single broker instance.
     */
    private Map<Long, String> brokerAddrs;
    private String zoneName;
    private final Random random = new Random();

    /**
     * Enable acting master or not, used for old version HA adaption,
     */
    private boolean enableActingMaster = false;

    public GroupInfo(GroupInfo brokerData) {
        this.cluster = brokerData.getCluster();
        this.brokerName = brokerData.getBrokerName();
        if (brokerData.getBrokerAddrs() != null) {
            this.brokerAddrs = new HashMap<>(brokerData.getBrokerAddrs());
        }
        this.zoneName = brokerData.getZoneName();
        this.enableActingMaster = brokerData.isEnableActingMaster();
    }

    public GroupInfo(String cluster, String brokerName) {
        this.cluster = cluster;
        this.brokerName = brokerName;
    }

    public GroupInfo(String cluster, String brokerName, HashMap<Long, String> brokerAddrs) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
    }

    public GroupInfo(String cluster, String brokerName, HashMap<Long, String> brokerAddrs,
        boolean enableActingMaster) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
        this.enableActingMaster = enableActingMaster;
    }

    public GroupInfo(String cluster, String brokerName, HashMap<Long, String> brokerAddrs, boolean enableActingMaster,
        String zoneName) {
        this.cluster = cluster;
        this.brokerName = brokerName;
        this.brokerAddrs = brokerAddrs;
        this.enableActingMaster = enableActingMaster;
        this.zoneName = zoneName;
    }

    /**
     * Selects a (preferably master) broker address from the registered list. If the master's address cannot be found, a
     * slave broker address is selected in a random manner.
     *
     * @return Broker address.
     */
    public String selectBrokerAddr() {
        String masterAddress = this.brokerAddrs.get(MQConstants.MASTER_ID);

        if (masterAddress == null) {
            List<String> addrs = new ArrayList<>(brokerAddrs.values());
            return addrs.get(random.nextInt(addrs.size()));
        }

        return masterAddress;
    }

    public Long getMinNo() {
        if (brokerAddrs.isEmpty()) {
            return null;
        }

        return Collections.min(brokerAddrs.keySet());
    }

    public boolean containsNo(long groupNo) {
        return brokerAddrs.containsKey(groupNo);
    }

    public String getMasterAddr() {
        return brokerAddrs.get(MQConstants.MASTER_ID);
    }

    public boolean isAddressEmpty() {
        return brokerAddrs.isEmpty();
    }

    public String getAddress(long groupNo) {
        return brokerAddrs.get(groupNo);
    }

    public String putAddress(long brokerId, String brokerAddress) {
        return brokerAddrs.put(brokerId, brokerAddress);
    }

    @Override
    public int compareTo(GroupInfo o) {
        return brokerName.compareTo(o.brokerName);
    }
}
