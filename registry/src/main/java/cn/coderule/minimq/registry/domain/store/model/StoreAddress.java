package cn.coderule.minimq.registry.domain.store.model;

import java.io.Serializable;
import lombok.Data;

/**
 * replaced by StoreInfo
 */
@Deprecated
@Data
public class StoreAddress implements Serializable {
    private final String clusterName;
    private final String brokerAddr;

    private int hash;

    public StoreAddress(String clusterName, String brokerAddr) {
        this.clusterName = clusterName;
        this.brokerAddr = brokerAddr;
    }

    public boolean isEmpty() {
        return clusterName.isEmpty() && brokerAddr.isEmpty();
    }

}
