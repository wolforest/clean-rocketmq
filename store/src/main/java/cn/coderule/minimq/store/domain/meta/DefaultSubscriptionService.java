package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.domain.model.meta.SubscriptionMap;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSubscriptionService implements SubscriptionService {
    private final String storePath;
    private final ConsumeOffsetService consumeOffsetService;
    @Getter
    private SubscriptionMap subscriptionMap;

    public DefaultSubscriptionService(String storePath, ConsumeOffsetService consumeOffsetService) {
        this.storePath = storePath;
        this.consumeOffsetService = consumeOffsetService;

        subscriptionMap = new SubscriptionMap();
    }

    @Override
    public boolean existsGroup(String groupName) {
        return subscriptionMap.existsGroup(groupName);
    }

    @Override
    public SubscriptionGroup getGroup(String groupName) {
        return subscriptionMap.getGroup(groupName);
    }

    @Override
    public void putGroup(SubscriptionGroup group) {
        subscriptionMap.putGroup(group);
    }

    @Override
    public void saveGroup(SubscriptionGroup group) {
        subscriptionMap.saveGroup(group, StoreContext.getStateMachineVersion());
        this.store();
    }

    @Override
    public void deleteGroup(String groupName, boolean cleanOffset) {
        subscriptionMap.deleteGroup(groupName, StoreContext.getStateMachineVersion());
        this.store();

        if (!cleanOffset) {
            return;
        }

        consumeOffsetService.deleteByGroup(groupName);
    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            registerSystemGroup();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(subscriptionMap);
        FileUtil.stringToFile(data, storePath);
    }

    private void registerSystemGroup() {
        SystemGroupRegister register = new SystemGroupRegister(this);
        register.register();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            registerSystemGroup();
            return;
        }

        this.subscriptionMap = JSONUtil.parse(data, SubscriptionMap.class);
    }
}
