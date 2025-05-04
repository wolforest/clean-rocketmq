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

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.core.channel.common.CommonChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChannel extends CommonChannel {
    protected final SocketAddress remoteSocketAddress;
    protected final SocketAddress localSocketAddress;

    protected AbstractChannel(Channel parent, String remoteAddress,
        String localAddress) {
        super(parent, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

    protected AbstractChannel(Channel parent, ChannelId id, String remoteAddress,
        String localAddress) {
        super(parent, id, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

//    @Override
//    public ChannelFuture writeAndFlush(Object msg) {
//        CompletableFuture<Void> processFuture = new CompletableFuture<>();
//
//        try {
//            if (msg instanceof RpcCommand command) {
//                RequestContext context = RequestContext.createForInner(this.getClass())
//                    .setRemoteAddress(remoteAddress)
//                    .setLocalAddress(localAddress);
//                if (command.getExtFields() == null) {
//                    command.setExtFields(new HashMap<>());
//                }
//                switch (command.getCode()) {
//                    case RequestCode.CHECK_TRANSACTION_STATE: {
//                        CheckTransactionStateRequestHeader header = (CheckTransactionStateRequestHeader) command.readCustomHeader();
//                        MessageBO MessageBO = MessageDecoder.decode(ByteBuffer.wrap(command.getBody()), true, false, false);
//                        RelayData<TransactionData, Void> relayData = this.relayService.checkTransaction(context, command, header, MessageBO);
//                        processFuture = this.processCheckTransaction(header, MessageBO, relayData.getProcessResult(), relayData.getRelayFuture());
//                        break;
//                    }
//                    case RequestCode.GET_CONSUMER_RUNNING_INFO: {
//                        GetConsumerRunningInfoRequestHeader header = (GetConsumerRunningInfoRequestHeader) command.readCustomHeader();
//                        CompletableFuture<RelayResult<ConsumerRunningInfo>> relayFuture = this.relayService.getConsumerInfo(context, command, header);
//                        processFuture = this.processGetConsumerRunningInfo(command, header, relayFuture);
//                        break;
//                    }
//                    case RequestCode.CONSUME_MESSAGE_DIRECTLY: {
//                        ConsumeMessageDirectlyResultRequestHeader header = (ConsumeMessageDirectlyResultRequestHeader) command.readCustomHeader();
//                        MessageBO MessageBO = MessageDecoder.decode(ByteBuffer.wrap(command.getBody()), true, false, false);
//                        processFuture = this.processConsumeMessageDirectly(command, header, MessageBO,
//                            this.relayService.consumeMessage(context, command, header));
//                        break;
//                    }
//                    default:
//                        break;
//                }
//            } else {
//                processFuture = processOtherMessage(msg);
//            }
//        } catch (Throwable t) {
//            log.error("process failed. msg:{}", msg, t);
//            processFuture.completeExceptionally(t);
//        }
//
//        DefaultChannelPromise promise = new DefaultChannelPromise(this, GlobalEventExecutor.INSTANCE);
//        processFuture.thenAccept(ignore -> promise.setSuccess())
//            .exceptionally(t -> {
//                promise.setFailure(t);
//                return null;
//            });
//        return promise;
//    }

//    protected abstract CompletableFuture<Void> processOtherMessage(Object msg);
//
//    protected abstract CompletableFuture<Void> processCheckTransaction(
//        CheckTransactionStateRequestHeader header,
//        MessageBO MessageBO,
//        TransactionData transactionData,
//        CompletableFuture<RelayResult<Void>> responseFuture);
//
//    protected abstract CompletableFuture<Void> processGetConsumerRunningInfo(
//        RpcCommand command,
//        GetConsumerRunningInfoRequestHeader header,
//        CompletableFuture<RelayResult<ConsumerRunningInfo>> responseFuture);
//
//    protected abstract CompletableFuture<Void> processConsumeMessageDirectly(
//        RpcCommand command,
//        ConsumeMessageDirectlyResultRequestHeader header,
//        MessageBO MessageBO,
//        CompletableFuture<RelayResult<ConsumeMessageDirectlyResult>> responseFuture);

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }

    @Override
    protected io.netty.channel.AbstractChannel.AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return false;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {

    }

    @Override
    protected SocketAddress localAddress0() {
        return this.localSocketAddress;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return this.remoteSocketAddress;
    }
}
