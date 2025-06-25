package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;

public class SystemGroupRegister {
    private final SubscriptionService subscriptionService;

    public SystemGroupRegister(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public void register() {
        registerToolsConsumerGroup();
        registerFilterConsumerGroup();
        registerSelfTestConsumerGroup();
        registerOnsHttpProxyGroup();
        registerCidONSAPIPullGroup();
        registerCidONSAPIPermissionGroup();
        registerCidONSAPIOwnerGroup();
        registerCidSYSTransGroup();
    }

    private void registerToolsConsumerGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.TOOLS_CONSUMER_GROUP);
        subscriptionService.putGroup(group);
    }

    private void registerFilterConsumerGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.FILTERSRV_CONSUMER_GROUP);
        subscriptionService.putGroup(group);
    }

    private void registerSelfTestConsumerGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.SELF_TEST_CONSUMER_GROUP);
        subscriptionService.putGroup(group);
    }

    private void registerOnsHttpProxyGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.ONS_HTTP_PROXY_GROUP);
        group.setConsumeBroadcastEnable(true);
        subscriptionService.putGroup(group);
    }

    private void registerCidONSAPIPullGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.CID_ONSAPI_PULL_GROUP);
        group.setConsumeBroadcastEnable(true);
        subscriptionService.putGroup(group);
    }

    private void registerCidONSAPIPermissionGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.CID_ONSAPI_PERMISSION_GROUP);
        group.setConsumeBroadcastEnable(true);
        subscriptionService.putGroup(group);
    }

    private void registerCidONSAPIOwnerGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.CID_ONSAPI_OWNER_GROUP);
        group.setConsumeBroadcastEnable(true);
        subscriptionService.putGroup(group);
    }

    private void registerCidSYSTransGroup() {
        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName(MQConstants.CID_SYS_RMQ_TRANS);
        group.setConsumeBroadcastEnable(true);
        subscriptionService.putGroup(group);
    }

}
