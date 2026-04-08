package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import cn.coderule.wolfmq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.wolfmq.domain.core.enums.produce.ProducerEvent;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.producer.hook.ProducerListener;
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
