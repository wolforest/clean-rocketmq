package cn.coderule.minimq.registry.domain.store;

import java.io.Serializable;
import lombok.Data;

@Data
public class BrokerAddrInfo implements Serializable {
    private final String clusterName;
    private final String brokerAddr;

    private int hash;

    public BrokerAddrInfo(String clusterName, String brokerAddr) {
        this.clusterName = clusterName;
        this.brokerAddr = brokerAddr;
    }

    public boolean isEmpty() {
        return clusterName.isEmpty() && brokerAddr.isEmpty();
    }

}
