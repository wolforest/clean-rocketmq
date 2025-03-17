package cn.coderule.minimq.registry.domain.store.model;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreStatusInfo implements Serializable {
    Map<Long, String> brokerAddrs;
    String offlineBrokerAddr;
    String haBrokerAddr;
}
