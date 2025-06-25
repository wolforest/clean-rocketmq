package cn.coderule.minimq.broker.server.grpc.interceptor;

import apache.rocketmq.v2.Settings;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.core.enums.consume.ConsumerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ConsumerListener;
import cn.coderule.minimq.broker.server.grpc.service.channel.GrpcChannel;
import cn.coderule.minimq.broker.server.core.ChannelHelper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcConsumerListener implements ConsumerListener {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public GrpcConsumerListener(SettingManager settingManager, ChannelManager channelManager) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    @Override
    public void handle(ConsumerEvent event, String group, Object... args) {
        switch (event) {
            case REGISTER:
                handleRegisterEvent(event, group, args);
                break;
            case UNREGISTER:
                handleUnregisterEvent(event, group, args);
                break;
            default:
                break;
        }
    }

    private void handleRegisterEvent(ConsumerEvent event, String group, Object... args) {
        if (CollectionUtil.isEmpty(args)) {
            return;
        }

        if (!(args[1] instanceof ClientChannelInfo channelInfo)) {
            return;
        }

        Channel channel = channelInfo.getChannel();
        if (!ChannelHelper.isRemote(channel)) {
            return;
        }

        Settings settings = GrpcChannel.parseChannelExtendAttribute(channel);
        log.debug("save grpc, group:{}, channelInfo:{}, settings:{}",
            group, channelInfo, settings);
        if (settings == null) {
            return;
        }

        settingManager.updateSettings(channelInfo.getClientId(), settings);
    }

    private void handleUnregisterEvent(ConsumerEvent event, String group, Object... args) {
        if (CollectionUtil.isEmpty(args)) {
            return;
        }

        if (!(args[0] instanceof ClientChannelInfo channelInfo)) {
            return;
        }

        if (ChannelHelper.isRemote(channelInfo.getChannel())) {
            return;
        }

        GrpcChannel channel = channelManager.removeChannel(channelInfo.getClientId());
        log.info("remove grpc, group:{}, ClientChannelInfo:{}, removed:{}",
            group, channelInfo, channel != null);
    }
}
