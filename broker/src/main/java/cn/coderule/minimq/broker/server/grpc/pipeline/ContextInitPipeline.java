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
package cn.coderule.minimq.broker.server.grpc.pipeline;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import com.google.protobuf.GeneratedMessage;
import io.grpc.Context;
import io.grpc.Metadata;
import java.util.concurrent.TimeUnit;

public class ContextInitPipeline implements RequestPipeline {
    @Override
    public void execute(RequestContext context, Metadata headers, GeneratedMessage request) {
        Context ctx = Context.current();
        context.setRequestTime(System.currentTimeMillis())
            .setLocalAddress(getDefaultStringMetadataInfo(headers, GrpcConstants.LOCAL_ADDRESS))
            .setRemoteAddress(getDefaultStringMetadataInfo(headers, GrpcConstants.REMOTE_ADDRESS))
            .setClientID(getDefaultStringMetadataInfo(headers, GrpcConstants.CLIENT_ID))
            .setProtocolType(ChannelProtocolType.GRPC_V2.getName())
            .setLanguage(getDefaultStringMetadataInfo(headers, GrpcConstants.LANGUAGE))
            .setClientVersion(getDefaultStringMetadataInfo(headers, GrpcConstants.CLIENT_VERSION))
            .setAction(getDefaultStringMetadataInfo(headers, GrpcConstants.SIMPLE_RPC_NAME))
            .setNamespace(getDefaultStringMetadataInfo(headers, GrpcConstants.NAMESPACE_ID));

        if (ctx.getDeadline() != null) {
            context.setRemainingMs(ctx.getDeadline().timeRemaining(TimeUnit.MILLISECONDS));
        }
    }

    protected String getDefaultStringMetadataInfo(Metadata headers, Metadata.Key<String> key) {
        return StringUtil.defaultString(headers.get(key));
    }
}
