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
package cn.coderule.minimq.rpc.common.netty.connection;

import cn.coderule.minimq.rpc.common.netty.event.NettyEvent;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventType;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class ServerConnectionManager extends ChannelDuplexHandler {
    private final NettyEventExecutor eventExecutor;

    public ServerConnectionManager(NettyEventExecutor eventExecutor) {
        this.eventExecutor = eventExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
        super.channelActive(ctx);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
        super.channelInactive(ctx);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent event)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        if (!event.state().equals(IdleState.ALL_IDLE)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
        NettyHelper.close(ctx.channel());

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(
                new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel())
            );
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }

        NettyHelper.close(ctx.channel());
    }
}

