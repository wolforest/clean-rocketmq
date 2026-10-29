package com.wolfmq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionMap;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.SubscriptionService;
import com.wolfmq.domain.server.store.StoreState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSubscriptionService implements SubscriptionService {
    private final StoreState storeState;
    private final String storePath;
    private final ConsumeOffsetService consumeOffsetService;
    @Getter
    private SubscriptionMap subscriptionMap;

    public DefaultSubscriptionService(
        StoreState storeState,
        String storePath,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeState = storeState;
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
        subscriptionMap.saveGroup(group, storeState.getStateMachineVersion());
        this.store();
    }

    @Override
    public void deleteGroup(String groupName, boolean cleanOffset) {
        subscriptionMap.deleteGroup(groupName, storeState.getStateMachineVersion());
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
