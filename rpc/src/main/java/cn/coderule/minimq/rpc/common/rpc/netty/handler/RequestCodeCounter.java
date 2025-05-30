package cn.coderule.minimq.rpc.common.rpc.netty.handler;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * channel read & write counter
 */
@ChannelHandler.Sharable
public class RequestCodeCounter extends ChannelDuplexHandler {

    private final ConcurrentMap<Integer, LongAdder> inboundDistribution;
    private final ConcurrentMap<Integer, LongAdder> outboundDistribution;

    public RequestCodeCounter() {
        inboundDistribution = new ConcurrentHashMap<>();
        outboundDistribution = new ConcurrentHashMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof RpcCommand cmd) {
            countInbound(cmd.getCode());
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcCommand cmd) {
            countOutbound(cmd.getCode());
        }
        ctx.write(msg, promise);
    }

    private void countInbound(int requestCode) {
        LongAdder item = inboundDistribution.computeIfAbsent(requestCode, k -> new LongAdder());
        item.increment();
    }

    private void countOutbound(int responseCode) {
        LongAdder item = outboundDistribution.computeIfAbsent(responseCode, k -> new LongAdder());
        item.increment();
    }

    private Map<Integer, Long> getDistributionSnapshot(Map<Integer, LongAdder> countMap) {
        Map<Integer, Long> map = new HashMap<>(countMap.size());
        for (Map.Entry<Integer, LongAdder> entry : countMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().sumThenReset());
        }
        return map;
    }

    private String snapshotToString(Map<Integer, Long> distribution) {
        if (null != distribution && !distribution.isEmpty()) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<Integer, Long> entry : distribution.entrySet()) {
                if (0L == entry.getValue()) {
                    continue;
                }
                sb.append(first ? "" : ", ").append(entry.getKey()).append(":").append(entry.getValue());
                first = false;
            }
            if (first) {
                return null;
            }
            sb.append("}");
            return sb.toString();
        }
        return null;
    }

    public String getInBoundSnapshotString() {
        return this.snapshotToString(this.getDistributionSnapshot(this.inboundDistribution));
    }

    public String getOutBoundSnapshotString() {
        return this.snapshotToString(this.getDistributionSnapshot(this.outboundDistribution));
    }
}
