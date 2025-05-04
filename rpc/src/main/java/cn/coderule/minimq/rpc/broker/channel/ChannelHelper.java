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

import cn.coderule.minimq.rpc.common.core.channel.remote.RemoteChannel;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import io.netty.channel.Channel;

public class ChannelHelper {

    /**
     * judge channel is sync from other proxy or not
     *
     * @param channel channel
     * @return true if is sync from other proxy
     */
    public static boolean isRemote(Channel channel) {
        return channel instanceof RemoteChannel;
    }

    public static ChannelProtocolType getChannelProtocolType(Channel channel) {
        if (channel instanceof GrpcChannel) {
            return ChannelProtocolType.GRPC_V2;
        } else if (channel instanceof RpcChannel) {
            return ChannelProtocolType.REMOTING;
        } else if (channel instanceof RemoteChannel) {
            RemoteChannel remoteChannel = (RemoteChannel) channel;
            return remoteChannel.getType();
        }
        return ChannelProtocolType.UNKNOWN;
    }
}
