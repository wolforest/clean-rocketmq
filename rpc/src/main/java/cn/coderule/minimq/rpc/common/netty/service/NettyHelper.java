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
package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.config.RpcSystemConfig;
import cn.coderule.minimq.rpc.common.core.constant.AttributeKeys;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingConnectException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingSendRequestException;
import cn.coderule.minimq.rpc.common.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * netty related utils
 * - channel connect/close
 * - write request/response
 * - attribute getter/setter
 * - address getter/setter
 */
@Slf4j
public class NettyHelper {
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_CIDR_ALL = "0.0.0.0/0";

    public static final Map<Integer, String> REQUEST_CODE_MAP = parseRequestCode();
    public static final Map<Integer, String> RESPONSE_CODE_MAP = parseResponseCode();

    public static SocketChannel connect(SocketAddress remote) {
        return connect(remote, 1000 * 5);
    }

    public static SocketChannel connect(SocketAddress remote, final int timeoutMillis) {
        SocketChannel sc = null;
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(true);
            sc.socket().setSoLinger(false, -1);
            sc.socket().setTcpNoDelay(true);

            if (RpcSystemConfig.socketSndbufSize > 0) {
                sc.socket().setReceiveBufferSize(RpcSystemConfig.socketSndbufSize);
            }
            if (RpcSystemConfig.socketRcvbufSize > 0) {
                sc.socket().setSendBufferSize(RpcSystemConfig.socketRcvbufSize);
            }

            sc.socket().connect(remote, timeoutMillis);
            sc.configureBlocking(false);
            return sc;
        } catch (Exception e) {
            closeSocketChannel(sc);
        }

        return null;
    }

    public static void close(Channel channel) {
        final String addrRemote = NettyHelper.getRemoteAddr(channel);
        if ("".equals(addrRemote)) {
            channel.close();
            return;
        }

        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                    future.isSuccess());
            }
        });
    }

    public static RpcCommand writeRequest(final String addr, final RpcCommand request, final long timeoutMillis)
        throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, RemotingCommandException {

        boolean sendRequestOK = false;
        long beginTime = System.currentTimeMillis();
        SocketChannel socketChannel = openSocketChannel(addr);

        try {
            socketChannel.configureBlocking(true);
            //bugfix  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4614802
            socketChannel.socket().setSoTimeout((int) timeoutMillis);

            writeRequest(socketChannel, request, addr, beginTime, timeoutMillis);
            sendRequestOK = true;

            int size = getBufferSize(socketChannel, addr, beginTime, timeoutMillis);
            ByteBuffer byteBufferBody = ByteBuffer.allocate(size);
            readBuffer(byteBufferBody, socketChannel, addr, beginTime, timeoutMillis);
            byteBufferBody.flip();

            return RpcCommand.decode(byteBufferBody);
        } catch (IOException e) {
            return handleSyncException(e, sendRequestOK, addr, timeoutMillis);
        } finally {
            closeSocketChannel(socketChannel);
        }
    }

    public static void writeResponse(Channel channel, RpcCommand request, @Nullable RpcCommand response) {
        writeResponse(channel, request, response, null);
    }

    public static void writeResponse(Channel channel, RpcCommand request, @Nullable RpcCommand response,
        Consumer<Future<?>> callback) {
        if (response == null) {
            return;
        }

        if (request.isOnewayRPC()) {
            return;
        }

        response.setOpaque(request.getOpaque());
        response.markResponseType();

        try {
            channel.writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("Response[request code: {}, response code: {}, opaque: {}] is written to channel{}",
                        request.getCode(), response.getCode(), response.getOpaque(), channel);
                } else {
                    log.error("Failed to write response[request code: {}, response code: {}, opaque: {}] to channel{}",
                        request.getCode(), response.getCode(), response.getOpaque(), channel, future.cause());
                }

                if (callback != null) {
                    callback.accept(future);
                }
            });
        } catch (Throwable e) {
            log.error("process request over, but response failed. request: {}", request, e);
        }
    }

    public static <T> T getAttribute(AttributeKey<T> key, final Channel channel) {
        if (!channel.hasAttr(key)) {
            return null;
        }

        Attribute<T> attribute = channel.attr(key);
        return attribute.get();
    }

    public static <T> void setAttribute(final Channel channel, AttributeKey<T> attributeKey, T value) {
        if (channel == null) {
            return;
        }
        channel.attr(attributeKey).set(value);
    }

    public static String getRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        String addr = getProxyAddress(channel);
        if (StringUtils.isNotBlank(addr)) {
            return addr;
        }
        Attribute<String> att = channel.attr(AttributeKeys.REMOTE_ADDR_KEY);
        if (att == null) {
            // mocked in unit test
            return parseRemoteAddr(channel);
        }
        addr = att.get();
        if (addr == null) {
            addr = parseRemoteAddr(channel);
            att.set(addr);
        }
        return addr;
    }

    public static String getLocalAddr(final Channel channel) {
        SocketAddress remote = channel.localAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.isEmpty()) {
            return "";
        }

        int index = addr.lastIndexOf("/");
        if (index >= 0) {
            return addr.substring(index + 1);
        }

        return addr;
    }

    public static String getRequestCodeDesc(int code) {
        return REQUEST_CODE_MAP.getOrDefault(code, String.valueOf(code));
    }

    public static String getResponseCodeDesc(int code) {
        return RESPONSE_CODE_MAP.getOrDefault(code, String.valueOf(code));
    }

    /************************************* private methods start ***********************************/
    private static SocketChannel openSocketChannel(String addr) throws RemotingConnectException {
        SocketAddress socketAddress = NetworkUtil.toSocketAddress(addr);
        SocketChannel socketChannel = connect(socketAddress);
        if (null == socketChannel) {
            throw new RemotingConnectException(addr);
        }

        return socketChannel;
    }

    private static void closeSocketChannel(SocketChannel socketChannel) {
        if (socketChannel == null) {
            return;
        }

        try {
            socketChannel.close();
        } catch (IOException e) {
            log.error("close socketChannel exception", e);
        }
    }

    private static void readBuffer(ByteBuffer byteBufferBody, SocketChannel socketChannel, String addr, long beginTime, long timeout) throws RemotingSendRequestException, IOException, InterruptedException {
        while (byteBufferBody.hasRemaining()) {
            int length = socketChannel.read(byteBufferBody);
            checkRequestResult(length, addr, beginTime, timeout, byteBufferBody);

            ThreadUtil.sleep(1);
        }
    }

    private static int getBufferSize(SocketChannel socketChannel, String addr, long beginTime, long timeout) throws RemotingSendRequestException, IOException, InterruptedException {
        ByteBuffer byteBufferSize = ByteBuffer.allocate(4);
        while (byteBufferSize.hasRemaining()) {
            int length = socketChannel.read(byteBufferSize);
            checkRequestResult(length, addr, beginTime, timeout, byteBufferSize);

            ThreadUtil.sleep(1);
        }

        return byteBufferSize.getInt(0);
    }

    private static void writeRequest(SocketChannel socketChannel, RpcCommand request, String addr, long beginTime, long timeout) throws RemotingSendRequestException, IOException, InterruptedException {
        ByteBuffer byteBufferRequest = request.encode();
        while (byteBufferRequest.hasRemaining()) {
            int length = socketChannel.write(byteBufferRequest);
            checkRequestResult(length, addr, beginTime, timeout, byteBufferRequest);

            ThreadUtil.sleep(1);
        }
    }

    private static void checkRequestResult(int length, String addr, long beginTime, long timeout, ByteBuffer buffer) throws RemotingSendRequestException {
        if (length <= 0) {
            throw new RemotingSendRequestException(addr);
        }

        if (!buffer.hasRemaining()) {
            return;
        }

        if ((System.currentTimeMillis() - beginTime) > timeout) {
            throw new RemotingSendRequestException(addr);
        }
    }

    private static RpcCommand handleSyncException(IOException e, boolean sendRequestOK, String addr, long timeoutMillis) throws RemotingTimeoutException, RemotingSendRequestException {
        log.error("invokeSync failure", e);

        if (sendRequestOK) {
            throw new RemotingTimeoutException(addr, timeoutMillis);
        } else {
            throw new RemotingSendRequestException(addr);
        }
    }

    private static String getProxyAddress(Channel channel) {
        if (!channel.hasAttr(AttributeKeys.PROXY_PROTOCOL_ADDR)) {
            return null;
        }

        String proxyProtocolAddr = getAttribute(AttributeKeys.PROXY_PROTOCOL_ADDR, channel);
        String proxyProtocolPort = getAttribute(AttributeKeys.PROXY_PROTOCOL_PORT, channel);
        if (StringUtils.isBlank(proxyProtocolAddr) || proxyProtocolPort == null) {
            return null;
        }
        return proxyProtocolAddr + ":" + proxyProtocolPort;
    }

    private static String parseRemoteAddr(final Channel channel) {
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.isEmpty()) {
            return "";
        }

        int index = addr.lastIndexOf("/");
        if (index >= 0) {
            return addr.substring(index + 1);
        }

        return addr;
    }

    private static Map<Integer, String> parseRequestCode() {
        Map<Integer, String> map = new HashMap<>();
        try {
            Field[] f = RequestCode.class.getFields();
            for (Field field : f) {
                if (field.getType() == int.class) {
                    map.put((int) field.get(null), field.getName().toLowerCase());
                }
            }
        } catch (IllegalAccessException ignore) {
        }

        return map;
    }

    private static Map<Integer, String> parseResponseCode() {
        Map<Integer, String> map = new HashMap<>();
        try {
            Field[] f = ResponseCode.class.getFields();
            for (Field field : f) {
                if (field.getType() == int.class) {
                    map.put((int) field.get(null), field.getName().toLowerCase());
                }
            }
        } catch (IllegalAccessException ignore) {
        }

        return map;
    }
}
