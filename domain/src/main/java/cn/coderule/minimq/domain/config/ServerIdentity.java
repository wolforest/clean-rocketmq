package cn.coderule.minimq.domain.config;

import cn.coderule.common.util.lang.BeanUtil;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import java.io.Serializable;
import lombok.Data;

@Data
public class ServerIdentity implements Serializable {
    private static final String DEFAULT_CLUSTER_NAME = "DefaultCluster";
    private static final String DEFAULT_GROUP_NAME = "DefaultGroup";
    private static final String DEFAULT_CONTAINER_NAME = "DefaultContainer";

    /**
     * store cluster name
     */
    private String cluster = DEFAULT_CLUSTER_NAME;
    /**
     * store (Master/slave) group name
     */
    private String group = DEFAULT_GROUP_NAME;
    /**
     * store group no
     *  - -1 : local
     *  - 0 : master
     *  - 1 ... : slave
     */
    private volatile long groupNo = MQConstants.MASTER_ID;;

    private boolean inContainer = false;
    private boolean isContainer = false;

    public String getName() {
        return isContainer
            ? DEFAULT_CONTAINER_NAME
            : cluster + "_" + group + "_" + groupNo;
    }

    public String getIdentity() {
        return "#" + getName() + "#";
    }

    public boolean isMaster() {
        return groupNo == MQConstants.MASTER_ID;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerIdentity that = (ServerIdentity) o;
        return BeanUtil.equalsBuilder()
            .append(cluster, that.cluster)
            .append(group, that.group)
            .append(groupNo, that.groupNo)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return BeanUtil.hashCodeBuilder()
            .append(cluster)
            .append(group)
            .append(groupNo)
            .toHashCode();
    }
}
