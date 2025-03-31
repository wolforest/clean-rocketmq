package cn.coderule.minimq.domain.model.meta;

import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.model.subscription.SubscriptionGroupAttributes;
import cn.coderule.minimq.domain.utils.attribute.AttributeUtil;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class SubscriptionMap implements Serializable {
    private ConcurrentHashMap<String, SubscriptionGroup> subscriptionGroupTable;
    private ConcurrentMap<String, ConcurrentMap<String, Integer>> forbiddenTable;
    private DataVersion dataVersion;

    public SubscriptionMap() {
        this.subscriptionGroupTable = new ConcurrentHashMap<>(1024);
        this.forbiddenTable = new ConcurrentHashMap<>(4);
        this.dataVersion = new DataVersion();
    }

    public boolean existsGroup(String groupName) {
        return subscriptionGroupTable.containsKey(groupName);
    }

    public void deleteGroup(String groupName, long stateVersion) {
        SubscriptionGroup old = subscriptionGroupTable.remove(groupName);
        if (old == null) {
            log.info("fail to delete group, groupName: {}", groupName);
            return;
        }

        log.info("Delete Group: {}", old);
        dataVersion.nextVersion(stateVersion);
    }

    public SubscriptionGroup getGroup(String groupName) {
        return subscriptionGroupTable.get(groupName);
    }

    public void putGroup(SubscriptionGroup group) {
        SubscriptionGroup old = subscriptionGroupTable.put(group.getGroupName(), group);
        if (old == null) {
            log.info("New Group: {}", group);
        } else  {
            log.info("Group changed, Old: {}; NEW: {}", old, group);
        }
    }

    public void saveGroup(SubscriptionGroup group, long stateVersion) {
       setAttributes(group);
       putGroup(group);
       dataVersion.nextVersion(stateVersion);
    }

    private void setAttributes(SubscriptionGroup group) {
        Map<String, String> newAttributes = getNewAttributes(group);
        Map<String, String> oldAttributes = getOldAttributes(group.getGroupName());
        Map<String, String> attributes = AttributeUtil.alterCurrentAttributes(
            existsGroup(group.getGroupName()),
            SubscriptionGroupAttributes.ALL,
            ImmutableMap.copyOf(oldAttributes),
            ImmutableMap.copyOf(newAttributes)
        );
        group.setAttributes(attributes);
    }

    private Map<String, String> getNewAttributes(SubscriptionGroup group) {
        return null == group.getAttributes()
            ? new HashMap<>()
            : group.getAttributes();
    }

    private Map<String, String> getOldAttributes(String groupName) {
        SubscriptionGroup old = subscriptionGroupTable.get(groupName);
        if (old == null) {
            return new HashMap<>();
        }

        return null != old.getAttributes()
            ? old.getAttributes()
            : new HashMap<>();
    }
}
