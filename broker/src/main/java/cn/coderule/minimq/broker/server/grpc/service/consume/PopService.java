package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Settings;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.service.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.SettingManager;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {
    private final ConsumerController consumerController;
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public PopService(
        ConsumerController consumerController,
        SettingManager settingManager,
        ChannelManager channelManager
    ) {
        this.consumerController = consumerController;
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public CompletableFuture<ReceiveMessageResponse> receive(
        RequestContext context, ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
        ConsumeService consumeService = new ConsumeService(consumerController, responseObserver);
        Settings settings = settingManager.getSettings(context);

        return null;
    }
}
