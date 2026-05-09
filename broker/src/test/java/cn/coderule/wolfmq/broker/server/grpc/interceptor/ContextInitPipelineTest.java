package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.common.grpc.core.constants.GrpcConstants;
import com.google.protobuf.GeneratedMessage;
import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextInitPipelineTest {

    @Spy
    private ContextInitPipeline pipeline;

    @Mock
    private GeneratedMessage request;

    private Metadata headers;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        headers = new Metadata();
        context = new RequestContext();
    }

    @Test
    void execute_withAllHeadersPresent_setsContextFieldsCorrectly() {
        headers.put(GrpcConstants.LOCAL_ADDRESS, "127.0.0.1:8080");
        headers.put(GrpcConstants.REMOTE_ADDRESS, "192.168.1.100:54321");
        headers.put(GrpcConstants.CLIENT_ID, "test-client-001");
        headers.put(GrpcConstants.LANGUAGE, "JAVA");
        headers.put(GrpcConstants.CLIENT_VERSION, "5.0.0");
        headers.put(GrpcConstants.SIMPLE_RPC_NAME, "SendMessage");
        headers.put(GrpcConstants.NAMESPACE_ID, "default-namespace");

        pipeline.execute(context, headers, request);

        assertEquals("127.0.0.1:8080", context.getLocalAddress());
        assertEquals("192.168.1.100:54321", context.getRemoteAddress());
        assertEquals("test-client-001", context.getClientID());
        assertEquals("grpc_v2", context.getProtocolType());
        assertEquals("JAVA", context.getLanguage());
        assertEquals("5.0.0", context.getClientVersion());
        assertEquals("SendMessage", context.getAction());
        assertEquals("default-namespace", context.getNamespace());
        assertNotNull(context.getRequestTime());
    }

    @Test
    void execute_withNullMissingHeaders_setsFieldsToEmptyString() {
        Metadata emptyHeaders = new Metadata();

        pipeline.execute(context, emptyHeaders, request);

        assertEquals("", context.getLocalAddress());
        assertEquals("", context.getRemoteAddress());
        assertEquals("", context.getClientID());
        assertEquals("grpc_v2", context.getProtocolType());
        assertEquals("", context.getLanguage());
        assertEquals("", context.getClientVersion());
        assertEquals("", context.getAction());
        assertEquals("", context.getNamespace());
    }

    @Test
    void execute_withDeadline_setsRemainingMs() {
        headers.put(GrpcConstants.CLIENT_ID, "client-with-deadline");

        Deadline deadline = Deadline.after(5000, TimeUnit.MILLISECONDS);
        Context deadlineContext = Context.current().withDeadline(deadline);

        Context previous = deadlineContext.attach();
        try {
            pipeline.execute(context, headers, request);

            Long remainingMs = context.getRemainingMs();
            assertNotNull(remainingMs);
            assertTrue(remainingMs > 0 && remainingMs <= 5000,
                "remainingMs should be positive and <= 5000, but was: " + remainingMs);
        } finally {
            deadlineContext.detach(previous);
        }
    }

    @Test
    void execute_withoutDeadline_doesNotSetRemainingMs() {
        headers.put(GrpcConstants.CLIENT_ID, "client-no-deadline");

        pipeline.execute(context, headers, request);

        assertNull(context.getRemainingMs());
    }

    @Test
    void execute_setsProtocolTypeToGrpcV2() {
        pipeline.execute(context, headers, request);

        assertEquals("grpc_v2", context.getProtocolType());
    }

    @Test
    void execute_setsRequestTime() {
        long before = System.currentTimeMillis();

        pipeline.execute(context, headers, request);

        long requestTime = context.getRequestTime();
        assertNotNull(requestTime);
        assertTrue(requestTime >= before);
        assertTrue(requestTime <= System.currentTimeMillis());
    }

    @Test
    void execute_withPartialHeaders_setsProvidedAndDefaultsMissing() {
        headers.put(GrpcConstants.CLIENT_ID, "partial-client");
        headers.put(GrpcConstants.NAMESPACE_ID, "my-namespace");

        pipeline.execute(context, headers, request);

        assertEquals("partial-client", context.getClientID());
        assertEquals("my-namespace", context.getNamespace());
        assertEquals("", context.getLocalAddress());
        assertEquals("", context.getRemoteAddress());
        assertEquals("", context.getLanguage());
        assertEquals("", context.getClientVersion());
        assertEquals("", context.getAction());
    }
}
