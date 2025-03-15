package cn.coderule.minimq.registry.domain.store.model;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class StoreStatusInfo implements Serializable {
    Map<Long, String> brokerAddrs;
    String offlineBrokerAddr;
    String haBrokerAddr;
}
