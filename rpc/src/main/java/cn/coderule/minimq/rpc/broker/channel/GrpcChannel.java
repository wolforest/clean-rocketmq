/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    private final AtomicReference<StreamObserver<TelemetryCommand>> telemetryCommandRef = new AtomicReference<>();
    private final Object telemetryWriteLock = new Object();
    private final String clientId;

    public GrpcChannel(
        RequestContext ctx,
        String clientId
    ) {
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
        this.telemetryCommandRef.set(future);
    }

    protected void clearClientObserver(StreamObserver<TelemetryCommand> future) {
        this.telemetryCommandRef.compareAndSet(future, null);
    }

    @Override
    public boolean isOpen() {
        return this.telemetryCommandRef.get() != null;
    }

    @Override
    public boolean isActive() {
        return this.telemetryCommandRef.get() != null;
    }

    @Override
    public boolean isWritable() {
        return this.telemetryCommandRef.get() != null;
    }

    public String getClientId() {
        return clientId;
    }

    public void writeTelemetryCommand(TelemetryCommand command) {
        StreamObserver<TelemetryCommand> observer = this.telemetryCommandRef.get();
        if (observer == null) {
            log.warn("telemetry command observer is null when try to write data. command:{}, channel:{}", TextFormat.shortDebugString(command), this);
            return;
        }
        synchronized (this.telemetryWriteLock) {
            observer = this.telemetryCommandRef.get();
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
