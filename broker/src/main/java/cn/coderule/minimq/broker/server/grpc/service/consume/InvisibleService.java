package cn.coderule.minimq.broker.server.grpc.service.consume;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.rpc.broker.grpc.ChannelManager;
import cn.coderule.minimq.rpc.broker.grpc.SettingManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvisibleService {
    private final ConsumerController consumerController;
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public InvisibleService(
        ConsumerController consumerController,
        SettingManager settingManager,
        ChannelManager channelManager
    ) {
        this.consumerController = consumerController;
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

}
