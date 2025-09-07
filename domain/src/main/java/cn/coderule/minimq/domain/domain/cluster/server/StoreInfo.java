package cn.coderule.minimq.domain.domain.cluster.server;

import cn.coderule.minimq.domain.core.enums.RequestType;
import cn.coderule.minimq.domain.domain.meta.topic.TopicConfigSerializeWrapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfo extends ServerInfo {
    private String haAddress;
    private Boolean enableMasterElection;

    private TopicConfigSerializeWrapper topicInfo;
    private List<String> filterList;

    private Integer registerTimeout;
    @Builder.Default
    private RequestType registerType = RequestType.SYNC;
    @Builder.Default
    private boolean forceRegister = false;

    private int hash;
    private boolean compressed;

    public StoreInfo(String clusterName, String address) {
        this.clusterName = clusterName;
        this.address = address;
    }

    public boolean isEnableMasterElection() {
        return enableMasterElection != null && enableMasterElection;
    }

    public boolean isOneway() {
        return registerType == RequestType.ONEWAY;
    }

    public boolean isAsync() {
        return registerType == RequestType.ASYNC;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof StoreInfo addr) {
            return clusterName.equals(addr.clusterName)
                && address.equals(addr.address);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && clusterName.length() + address.length() > 0) {
            for (int i = 0; i < clusterName.length(); i++) {
                h = 31 * h + clusterName.charAt(i);
            }
            h = 31 * h + '_';
            for (int i = 0; i < address.length(); i++) {
                h = 31 * h + address.charAt(i);
            }
            hash = h;
        }
        return h;
    }

}
