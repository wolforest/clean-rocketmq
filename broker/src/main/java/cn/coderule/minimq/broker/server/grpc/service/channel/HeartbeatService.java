package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public HeartbeatService(SettingManager settingManager, ChannelManager channelManager) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public CompletableFuture<HeartbeatResponse> heartbeat(RequestContext context, HeartbeatRequest request) {
        try {
            Settings settings = settingManager.getSettings(context);
            if (settings == null) {
                return noSettings();
            }

            return process(context, request, settings);
        } catch (Throwable t) {
            return processError(t);
        }
    }

    private CompletableFuture<HeartbeatResponse> process(
        RequestContext context,
        HeartbeatRequest request,
        Settings settings
    ) {

        return null;
    }

    private CompletableFuture<HeartbeatResponse> notSupported(Settings settings) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, settings.getClientType().name());

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<HeartbeatResponse> noSettings() {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, "can't find client settings");

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<HeartbeatResponse> processError(Throwable t) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }
}
