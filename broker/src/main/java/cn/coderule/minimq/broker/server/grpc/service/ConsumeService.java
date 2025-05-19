package cn.coderule.minimq.broker.server.grpc.service;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.ResponseBuilder;
import cn.coderule.minimq.rpc.common.grpc.core.ResponseWriter;
import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
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
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setDeliveryTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
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
            case POLLING_FULL -> writeFullResult();
            default -> writeEmptyResult();
        }
    }


    private void writeFoundResult(RequestContext context, ReceiveMessageRequest request, PopResult popResult) {
        if (popResult.isEmpty()) {
            writeEmptyResult();
            return;
        }

        writeOkResult();

    }

    private void writeOkResult() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.OK, Code.OK.name());
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }

    private void writeFullResult() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.TOO_MANY_REQUESTS, "polling full");
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }

    private void writeEmptyResult() {
        Status status = ResponseBuilder.getInstance().buildStatus(Code.MESSAGE_NOT_FOUND, "no new message");
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        streamObserver.onNext(response);
    }
}
