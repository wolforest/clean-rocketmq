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
package cn.coderule.minimq.rpc.common.netty.codec;

import cn.coderule.minimq.rpc.common.netty.service.HAProxyMessageHandler;
import cn.coderule.minimq.rpc.common.netty.service.TlsModeHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ProtocolDetectionResult;
import io.netty.handler.codec.ProtocolDetectionState;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import io.netty.handler.codec.haproxy.HAProxyProtocolVersion;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HandshakeDecoder extends ByteToMessageDecoder {

    public static final String HANDSHAKE_HANDLER_NAME = "handshakeHandler";
    public static final String HA_PROXY_DECODER = "HAProxyDecoder";
    public static final String HA_PROXY_HANDLER = "HAProxyHandler";

    private final EventExecutorGroup eventExecutorGroup;
    private final TlsModeHandler tlsModeHandler;

    public HandshakeDecoder(EventExecutorGroup eventExecutorGroup, TlsModeHandler tlsModeHandler) {
        this.eventExecutorGroup = eventExecutorGroup;
        this.tlsModeHandler = tlsModeHandler;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        try {
            ProtocolDetectionResult<HAProxyProtocolVersion> detectionResult = HAProxyMessageDecoder.detectProtocol(byteBuf);
            if (detectionResult.state() == ProtocolDetectionState.NEEDS_MORE_DATA) {
                return;
            }
            if (detectionResult.state() == ProtocolDetectionState.DETECTED) {
                ctx.pipeline().addAfter(eventExecutorGroup, ctx.name(), HA_PROXY_DECODER, new HAProxyMessageDecoder())
                    .addAfter(eventExecutorGroup, HA_PROXY_DECODER, HA_PROXY_HANDLER, new HAProxyMessageHandler())
                    .addAfter(eventExecutorGroup, HA_PROXY_HANDLER, TlsModeHandler.TLS_MODE_HANDLER, tlsModeHandler);
            } else {
                ctx.pipeline().addAfter(eventExecutorGroup, ctx.name(), TlsModeHandler.TLS_MODE_HANDLER, tlsModeHandler);
            }

            try {
                // Remove this service
                ctx.pipeline().remove(this);
            } catch (NoSuchElementException e) {
                log.error("Error while removing HandshakeHandler", e);
            }
        } catch (Exception e) {
            log.error("process proxy protocol negotiator failed.", e);
            throw e;
        }
    }
}

