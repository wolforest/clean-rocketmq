package cn.coderule.minimq.rpc.broker.channel;

import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.channel.ChannelExtendAttributeGetter;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import cn.coderule.minimq.rpc.common.grpc.core.GrpcChannelId;
import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.netty.channel.Channel;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcChannel extends AbstractChannel {
    private final AtomicReference<StreamObserver<TelemetryCommand>> commandRef = new AtomicReference<>();
    private final Object telemetryWriteLock = new Object();
    private final String clientId;

    public GrpcChannel(RequestContext ctx, String clientId) {
        super(
            null,
            new GrpcChannelId(clientId),
            ctx.getRemoteAddress(),
            ctx.getLocalAddress()
        );

        this.clientId = clientId;
    }

    public static Settings parseChannelExtendAttribute(Channel channel) {
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(channel);
        if (!type.equals(ChannelProtocolType.GRPC_V2)) {
            return null;
        }

        if (!(channel instanceof ChannelExtendAttributeGetter)) {
            return null;
        }

        String attr = ((ChannelExtendAttributeGetter) channel).getChannelExtendAttribute();
        if (attr == null) {
            return null;
        }

        Settings.Builder builder = Settings.newBuilder();
        try {
            JsonFormat.parser().merge(attr, builder);
            return builder.build();
        } catch (InvalidProtocolBufferException e) {
            log.error("convert settings json data to settings failed. data:{}", attr, e);
            return null;
        }
    }

    public void setClientObserver(StreamObserver<TelemetryCommand> future) {
        this.commandRef.set(future);
    }

    protected void clearClientObserver(StreamObserver<TelemetryCommand> future) {
        this.commandRef.compareAndSet(future, null);
    }

    @Override
    public boolean isOpen() {
        return this.commandRef.get() != null;
    }

    @Override
    public boolean isActive() {
        return this.commandRef.get() != null;
    }

    @Override
    public boolean isWritable() {
        return this.commandRef.get() != null;
    }

    public String getClientId() {
        return clientId;
    }

    public void writeTelemetryCommand(TelemetryCommand command) {
        StreamObserver<TelemetryCommand> observer = this.commandRef.get();
        if (observer == null) {
            log.warn("telemetry command observer is null when try to write data. command:{}, channel:{}", TextFormat.shortDebugString(command), this);
            return;
        }
        synchronized (this.telemetryWriteLock) {
            observer = this.commandRef.get();
            if (observer == null) {
                log.warn("telemetry command observer is null when try to write data. command:{}, channel:{}", TextFormat.shortDebugString(command), this);
                return;
            }
            try {
                observer.onNext(command);
            } catch (StatusRuntimeException | IllegalStateException exception) {
                log.warn("write telemetry failed. command:{}", command, exception);
                this.clearClientObserver(observer);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("clientId", clientId)
            .add("remoteAddress", getRemoteAddress())
            .add("localAddress", getLocalAddress())
            .toString();
    }
}
