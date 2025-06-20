package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;

public class TerminationService {

    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public TerminationService(SettingManager settingManager, ChannelManager channelManager) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public void inject(ProducerController producerController, ConsumerController consumerController) {
        this.producerController = producerController;
        this.consumerController = consumerController;
    }

    public CompletableFuture<NotifyClientTerminationResponse> terminate(RequestContext context, NotifyClientTerminationRequest request) {
        return CompletableFuture.completedFuture(null);
    }

    private void notSupported(Settings settings) {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, settings.getClientType().name());

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

    }

    private CompletableFuture<NotifyClientTerminationResponse> success() {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.OK, Code.OK.name());

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<NotifyClientTerminationResponse> noSettings() {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, "can't find client settings");

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<NotifyClientTerminationResponse> processError(Throwable t) {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

}
