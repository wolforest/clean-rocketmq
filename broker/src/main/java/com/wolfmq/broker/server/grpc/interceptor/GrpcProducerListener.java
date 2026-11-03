package com.wolfmq.broker.server.grpc.interceptor;

import com.wolfmq.broker.server.grpc.service.channel.ChannelManager;
import com.wolfmq.broker.server.grpc.service.channel.SettingManager;
import com.wolfmq.common.core.event.ProducerEvent;
import com.wolfmq.common.server.broker.ClientChannelInfo;
import com.wolfmq.common.domain.producer.hook.ProducerListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcProducerListener implements ProducerListener {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public GrpcProducerListener(SettingManager settingManager, ChannelManager channelManager) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    @Override
    public void handle(ProducerEvent event, String group, ClientChannelInfo channelInfo) {
        if (event != ProducerEvent.CLIENT_UNREGISTER) {
            return;
        }

        channelManager.removeChannel(channelInfo.getClientId());
        settingManager.removeSettings(channelInfo.getClientId());
    }
}
