package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.SendResultEntry;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProducerConverter {
    public static SendMessageResponse toSendMessageResponse(
        RequestContext ctx, SendMessageRequest request, List<EnqueueResult> resultList) {
        SendMessageResponse.Builder builder = SendMessageResponse.newBuilder();

        Set<Code> responseCodes = new HashSet<>();
        for (EnqueueResult result : resultList) {
            SendResultEntry resultEntry = createSendResultEntry(result);
            builder.addEntries(resultEntry);
            responseCodes.add(resultEntry.getStatus().getCode());
        }

        setStatus(builder, responseCodes);
        return builder.build();
    }

    private static SendResultEntry createSendResultEntry(EnqueueResult result) {
        return switch (result.getStatus()) {
            case FLUSH_DISK_TIMEOUT -> SendResultEntry.newBuilder()
                .setStatus(ResponseBuilder.getInstance().buildStatus(
                    Code.MASTER_PERSISTENCE_TIMEOUT,
                    "send message failed, sendStatus=" + result.getStatus())
                )
                .build();
            case FLUSH_SLAVE_TIMEOUT -> SendResultEntry.newBuilder()
                .setStatus(ResponseBuilder.getInstance().buildStatus(
                    Code.SLAVE_PERSISTENCE_TIMEOUT,
                    "send message failed, sendStatus=" + result.getStatus())
                )
                .build();
            case SLAVE_NOT_AVAILABLE -> SendResultEntry.newBuilder()
                .setStatus(ResponseBuilder.getInstance().buildStatus(
                    Code.HA_NOT_AVAILABLE,
                    "send message failed, sendStatus=" + result.getStatus())
                )
                .build();
            case PUT_OK -> SendResultEntry.newBuilder()
                .setStatus(ResponseBuilder.getInstance().buildStatus(Code.OK, Code.OK.name()))
                .setOffset(result.getCommitOffset())
                .setMessageId(StringUtil.defaultString(result.getMessageId()))
                .setTransactionId(StringUtil.defaultString(result.getTransactionId()))
                .build();
            default -> SendResultEntry.newBuilder()
                .setStatus(ResponseBuilder.getInstance().buildStatus(
                    Code.INTERNAL_SERVER_ERROR,
                    "send message failed, sendStatus=" + result.getStatus())
                )
                .build();
        };
    }

    private static void setStatus(SendMessageResponse.Builder builder, Set<Code> responseCodes) {
        if (responseCodes.size() > 1) {
            builder.setStatus(ResponseBuilder.getInstance().buildStatus(Code.MULTIPLE_RESULTS, Code.MULTIPLE_RESULTS.name()));
        } else if (responseCodes.size() == 1) {
            Code code = responseCodes.stream().findAny().get();
            builder.setStatus(ResponseBuilder.getInstance().buildStatus(code, code.name()));
        } else {
            builder.setStatus(ResponseBuilder.getInstance().buildStatus(Code.INTERNAL_SERVER_ERROR, "send status is empty"));
        }
    }

}
