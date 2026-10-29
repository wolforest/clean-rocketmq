package com.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.Status;
import com.wolfmq.broker.api.ProducerController;
import com.wolfmq.broker.server.grpc.converter.MessageConverter;
import com.wolfmq.broker.server.grpc.converter.ProducerConverter;
import com.wolfmq.broker.server.grpc.interceptor.PipelineExecutor;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.rpc.common.grpc.activity.ActivityHelper;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

/**
 * producer activity
 *  - produce
 *  - moveToDLQ
 */
public class ProducerActivity {
    private final ThreadPoolExecutor executor;
    /**
     * inject by GrpcManager , while starting
     *  All controllers will be registered in ApplicationContext
     *      after related component initialized
     *  GrpcManager will get controllers in ApplicationContext
     */
    @Setter
    private ProducerController producerController;
    private final PipelineExecutor pipelineExecutor;


    public ProducerActivity(ThreadPoolExecutor executor, PipelineExecutor pipelineExecutor) {
        this.executor = executor;
        this.pipelineExecutor = pipelineExecutor;
    }

    /**
     * produce message
     *  - accept request
     *  - submit async task to thread pool
     *
     * @param request request
     * @param responseObserver response
     */
    public void produce(RequestContext context, SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        ActivityHelper<SendMessageRequest, SendMessageResponse> helper
            = getProduceHelper(context, request, responseObserver);

        try {
            pipelineExecutor.execute(context, request);

            Runnable task = ()
                -> produceAsync(context, request)
                .whenComplete(helper::writeResponse);
            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    /**
     * move to dead letter queue
     * @param request request
     * @param responseObserver response
     */
    public void moveToDLQ(RequestContext context, ForwardMessageToDeadLetterQueueRequest request, StreamObserver<ForwardMessageToDeadLetterQueueResponse> responseObserver) {
        ActivityHelper<ForwardMessageToDeadLetterQueueRequest, ForwardMessageToDeadLetterQueueResponse> helper
            = getMoveToDLQHelper(context, request, responseObserver);

        try {
            pipelineExecutor.execute(context, request);

            Runnable task = ()
                -> moveToDLQAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    private CompletableFuture<ForwardMessageToDeadLetterQueueResponse> moveToDLQAsync(RequestContext context, ForwardMessageToDeadLetterQueueRequest request) {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * async produce message
     *  - convert request to messageBO
     *  - call producerController.produce
     *  - convert messageBO to response
     *
     * @param request request
     * @return response
     */
    private CompletableFuture<SendMessageResponse> produceAsync(RequestContext context, SendMessageRequest request) {
        CompletableFuture<SendMessageResponse> future = new CompletableFuture<>();

        try {
            assertNotEmpty(request);
            List<MessageBO> messageBOList = MessageConverter.toMessageBO(context, request);

            return producerController.produce(context, messageBOList)
                .thenApply(ProducerConverter::toSendMessageResponse);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    private void assertNotEmpty(SendMessageRequest request) {
        int messageCount = request.getMessagesCount();
        if (messageCount > 0) {
            return;
        }

        throw new InvalidRequestException(InvalidCode.MESSAGE_CORRUPTED, "no message to send");
    }

    private Function<Status, ForwardMessageToDeadLetterQueueResponse> moveToDLQStatusToResponse() {
        return status -> ForwardMessageToDeadLetterQueueResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private ActivityHelper<ForwardMessageToDeadLetterQueueRequest, ForwardMessageToDeadLetterQueueResponse> getMoveToDLQHelper(
        RequestContext context,
        ForwardMessageToDeadLetterQueueRequest request,
        StreamObserver<ForwardMessageToDeadLetterQueueResponse> responseObserver
    ) {
        Function<Status, ForwardMessageToDeadLetterQueueResponse> statusToResponse = moveToDLQStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, SendMessageResponse> produceStatusToResponse() {
        return status -> SendMessageResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private ActivityHelper<SendMessageRequest, SendMessageResponse> getProduceHelper(
        RequestContext context,
        SendMessageRequest request,
        StreamObserver<SendMessageResponse> responseObserver
    ) {
        Function<Status, SendMessageResponse> statusToResponse = produceStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }
}
