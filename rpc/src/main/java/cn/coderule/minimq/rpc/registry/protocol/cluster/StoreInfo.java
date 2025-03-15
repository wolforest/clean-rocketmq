package cn.coderule.minimq.rpc.registry.protocol.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StoreInfo extends ServerInfo {
    private String haAddress;
    private Boolean enableActingMaster;

    public StoreInfo(String clusterName, String address) {
        this.clusterName = clusterName;
        this.address = address;
    }

    public boolean isEnableActingMaster() {
        return enableActingMaster != null && enableActingMaster;
    }
}
