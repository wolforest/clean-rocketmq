package cn.coderule.minimq.domain.model.meta;

import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;
import java.io.Serializable;
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
}
