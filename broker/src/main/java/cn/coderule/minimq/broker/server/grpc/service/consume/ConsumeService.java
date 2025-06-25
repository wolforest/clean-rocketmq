package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.converter.GrpcConverter;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseWriter;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeService {
    private final ConsumerController consumerController;
    private final StreamObserver<ReceiveMessageResponse> streamObserver;

    public ConsumeService(ConsumerController consumerController, StreamObserver<ReceiveMessageResponse> streamObserver) {
        this.consumerController = consumerController;
        this.streamObserver = streamObserver;
    }

    public void writeResponse(RequestContext context, ReceiveMessageRequest request, PopResult popResult) {
        try {
            writeByStatus(context, request, popResult);
        } catch (Throwable t) {
            writeResponse(context, t);
        } finally {
            onComplete();
        }
    }

    public void writeResponse(RequestContext context, Code code, String message) {
        Status status = ResponseBuilder.getInstance().buildStatus(code, message);
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();

        writeResponse(response);
        onComplete();
    }

    public void writeResponse(RequestContext context, Throwable t) {
        writeResponse(context, t, null);
    }

    public void writeResponse(RequestContext context, Throwable t, ReceiveMessageRequest request) {
        Status status = ResponseBuilder.getInstance().buildStatus(t);
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();

        writeResponse(response);
        onComplete();
    }

    private void writeResponse(ReceiveMessageResponse response) {
        try {
            ResponseWriter.getInstance().writeResponse(streamObserver, response);
        } catch (Exception e) {
            log.error("write ReceiveMessageResponse error", e);
        }
    }

    private void onComplete() {
        Timestamp timestamp = Timestamps.fromMillis(System.currentTimeMillis());
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setDeliveryTimestamp(timestamp)
            .build();
        writeResponse(response);

        try {
            streamObserver.onCompleted();
        } catch (Exception e) {
            log.error("err when complete receive message response", e);
        }
    }

    private void writeByStatus(RequestContext context, ReceiveMessageRequest request, PopResult popResult) {
        switch (popResult.getPopStatus()) {
            case FOUND -> writeFoundResult(context, request, popResult);
            case POLLING_FULL -> writeFullStatus();
            default -> writeEmptyStatus();
        }
    }

    private void writeFoundResult(RequestContext context, ReceiveMessageRequest request, PopResult popResult) {
        if (popResult.isEmpty()) {
            writeEmptyStatus();
            return;
        }

        writeOkStatus();
        writeMessageList(context, request, popResult);
    }

    private void writeMessageList(RequestContext context, ReceiveMessageRequest request, PopResult popResult) {
        Iterator<MessageBO> iterator = popResult.getMsgFoundList().iterator();
        while (iterator.hasNext()) {
            Throwable t = writeMessage(context, request, iterator.next());
            if (t == null) {
                continue;
            }

            iterator.forEachRemaining(messageBO -> {
                changeInvisible(context, messageBO, t);
            });
            break;
        }
    }

    private Throwable writeMessage(RequestContext context, ReceiveMessageRequest request, MessageBO messageBO) {
        Message message = GrpcConverter.getInstance().buildMessage(messageBO);
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setMessage(message)
            .build();

        try {
            streamObserver.onNext(response);
        } catch (Throwable t) {
            changeInvisible(context, messageBO, t);
            return t;
        }

        return null;
    }

    private void changeInvisible(RequestContext context, MessageBO messageBO, Throwable t) {
        String handle = messageBO.getProperty(MessageConst.PROPERTY_POP_CK);
        if (handle == null) {
            return;
        }

        InvisibleRequest invisibleRequest = InvisibleRequest.builder()
            .requestContext(context)
            .receiptHandle(ReceiptHandle.decode(handle))
            .messageId(messageBO.getMsgId())
            .topicName(messageBO.getTopic())
            .build();
        consumerController.changeInvisible(context, invisibleRequest);
    }

    private void writeOkStatus() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.OK, Code.OK.name());
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }

    private void writeFullStatus() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.TOO_MANY_REQUESTS, "polling full");
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }

    private void writeEmptyStatus() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.MESSAGE_NOT_FOUND, "no new message");
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }
}
