package cn.coderule.minimq.broker.server.grpc.service.channel;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.GrpcConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ResultFuture;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelManager implements Lifecycle {
    private final GrpcConfig grpcConfig;
    private final RelayService relayService;
    private final SettingManager settingManager;

    private final AtomicLong idGenerator;
    private final ConcurrentMap<String, GrpcChannel> channelMap;
    private final ConcurrentMap<String, ResultFuture> resultMap;

    private final ScheduledExecutorService scheduler;

    public ChannelManager(GrpcConfig grpcConfig, RelayService relayService, SettingManager settingManager) {
        this.grpcConfig = grpcConfig;
        this.relayService = relayService;
        this.settingManager = settingManager;

        this.idGenerator = new AtomicLong(0);
        this.channelMap = new ConcurrentHashMap<>();
        this.resultMap = new ConcurrentHashMap<>();

        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("GrpcChannelManager_")
        );
    }

    @Override
    public void initialize() throws Exception {
        this.scheduler.scheduleAtFixedRate(
            this::scanExpiredResult,
            10,
            1,
            TimeUnit.SECONDS
        );
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.scheduler.shutdown();
    }

    public GrpcChannel createChannel(RequestContext context, String clientId) {
        return this.channelMap.computeIfAbsent(
            clientId,
            k -> {
                GrpcChannel channel = new GrpcChannel(context, clientId);
                channel.setChannelManager(this);
                channel.setSettingManager(settingManager);
                channel.setRelayService(relayService);
                return channel;
            }
        );
    }

    public GrpcChannel getChannel(String clientId) {
        return this.channelMap.get(clientId);
    }

    public GrpcChannel removeChannel(String clientId) {
        return this.channelMap.remove(clientId);
    }

    public <T> String addResult(CompletableFuture<Result<T>> resultFuture) {
        String id = String.valueOf(this.idGenerator.incrementAndGet());
        this.resultMap.put(
            id,
            new ResultFuture<>(resultFuture)
        );

        return id;
    }

    public <T> CompletableFuture<Result<T>> getAndRemoveResult(String id) {
        @SuppressWarnings("unchecked")
        ResultFuture<T> resultFuture = this.resultMap.remove(id);

        return resultFuture == null
            ? null
            : resultFuture.getFuture();
    }

    private void scanExpiredResult() {
        Set<String> idSet = this.resultMap.keySet();
        for (String id : idSet) {
            ResultFuture<?> resultFuture = this.resultMap.get(id);
            if (resultFuture == null) {
                continue;
            }

            long now = System.currentTimeMillis();
            if (now - resultFuture.getCreateTime() <= grpcConfig.getRelayTimeout()) {
                continue;
            }

            resultFuture.getFuture().complete(
                new Result<>(ResponseCode.SYSTEM_BUSY,"grpc request timeout", null)
            );
        }
    }


}
