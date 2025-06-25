package cn.coderule.minimq.broker.server.grpc.interceptor;

import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.domain.core.enums.produce.ProducerEvent;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.service.broker.listener.ProducerListener;
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
