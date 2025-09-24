
package cn.coderule.minimq.broker.server.core;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.rpc.common.core.channel.mock.MockChannel;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ClientChannel extends MockChannel {
    protected final SocketAddress remoteSocketAddress;
    protected final SocketAddress localSocketAddress;

    protected ClientChannel(Channel parent, String remoteAddress,
        String localAddress) {
        super(parent, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

    protected ClientChannel(Channel parent, ChannelId id, String remoteAddress,
        String localAddress) {
        super(parent, id, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

    public abstract RelayService getRelayService();
    public abstract ChannelManager getChannelManager();
    public abstract SettingManager getSettingManager();

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
