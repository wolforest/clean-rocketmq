package cn.coderule.minimq.domain.domain.model.cluster.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ClusterInfo implements Serializable {
    private Map<String/* groupName */, GroupInfo> brokerAddrTable;
    private Map<String/* clusterName */, Set<String/* storeName */>> clusterAddrTable;

    public List<String> getAllAddr(String cluster) {
        List<String> addrs = new ArrayList<>();
        if (null == clusterAddrTable || clusterAddrTable.isEmpty()) {
            return addrs;
        }

        if (!clusterAddrTable.containsKey(cluster)) {
            return addrs;
        }

        Set<String> brokerNames = clusterAddrTable.get(cluster);
        for (String brokerName : brokerNames) {
            GroupInfo groupInfo = brokerAddrTable.get(brokerName);
            if (null == groupInfo) {
                continue;
            }
            addrs.addAll(groupInfo.getBrokerAddrs().values());
        }

        return addrs;
    }

    public String[] retrieveAllAddrByCluster(String cluster) {
        List<String> addrs = getAllAddr(cluster);

        return addrs.toArray(new String[] {});
    }

    public String[] retrieveAllClusterNames() {
        return clusterAddrTable.keySet().toArray(new String[] {});
    }
}
