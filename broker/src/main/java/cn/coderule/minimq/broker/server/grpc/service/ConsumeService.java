package cn.coderule.minimq.broker.server.grpc.service;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.ResponseWriter;
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

    public void writeResponse(RequestContext context, ReceiveMessageRequest request, PopRequest popRequest) {

    }

    public void writeResponse(RequestContext context, Code code, String message) {

    }

    public void writeResponse(RequestContext context, ReceiveMessageRequest request, Throwable t) {

    }

    private void writeResponse(ReceiveMessageResponse response) {
        try {
            ResponseWriter.getInstance().writeResponse(streamObserver, response);
        } catch (Exception e) {
            log.error("write ReceiveMessageResponse error", e);
        }
    }
}
