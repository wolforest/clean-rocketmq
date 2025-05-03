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

package cn.coderule.minimq.rpc.common.core.channel.invocation;

import cn.coderule.minimq.rpc.common.core.channel.SimpleChannel;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import io.netty.channel.ChannelFuture;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvocationChannel extends SimpleChannel {
    private static final int DEFAULT_CHANNEL_EXPIRED_IN_SECONDS = 60;

    protected final ConcurrentMap<Integer, InvocationContextInterface> inFlightRequestMap;

    public InvocationChannel(String remoteAddress, String localAddress) {
        super(remoteAddress, localAddress);
        this.inFlightRequestMap = new ConcurrentHashMap<>();
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        if (msg instanceof RpcCommand responseCommand) {
            InvocationContextInterface context = inFlightRequestMap.remove(responseCommand.getOpaque());
            if (null != context) {
                context.handle(responseCommand);
            }
            inFlightRequestMap.remove(responseCommand.getOpaque());
        }
        return super.writeAndFlush(msg);
    }

    @Override
    public boolean isWritable() {
        return !inFlightRequestMap.isEmpty();
    }

    @Override
    public void registerInvocationContext(int opaque, InvocationContextInterface context) {
        inFlightRequestMap.put(opaque, context);
    }

    @Override
    public void eraseInvocationContext(int opaque) {
        inFlightRequestMap.remove(opaque);
    }

    @Override
    public void clearExpireContext() {
        Iterator<Map.Entry<Integer, InvocationContextInterface>> iterator = inFlightRequestMap.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Integer, InvocationContextInterface> entry = iterator.next();
            if (entry.getValue().expired(DEFAULT_CHANNEL_EXPIRED_IN_SECONDS)) {
                iterator.remove();
                count++;
                log.debug("An expired request is found, request: {}", entry.getValue());
            }
        }
        if (count > 0) {
            log.warn("[BUG] {} expired in-flight requests is cleaned.", count);
        }
    }
}
