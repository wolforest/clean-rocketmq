package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.Settings;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.common.grpc.channel.GrpcChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HeartbeatServiceTest {

    @Mock
    private SettingManager settingManager;

    @Mock
    private RegisterService registerService;

    @Mock
    private GrpcChannel mockChannel;

    private HeartbeatService heartbeatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        heartbeatService = new HeartbeatService(settingManager, registerService);
    }

    @Test
    void testHeartbeatWithNullSettings() {
        RequestContext context = RequestContext.create("testGroup");
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
            .setClientType(ClientType.PRODUCER)
            .build();

        when(settingManager.getSettings(context)).thenReturn(null);

        CompletableFuture<HeartbeatResponse> future = heartbeatService.heartbeat(context, request);
        HeartbeatResponse response = future.join();

        assertNotNull(response);
        assertEquals(Code.UNRECOGNIZED_CLIENT_TYPE, response.getStatus().getCode());
    }

    @Test
    void testHeartbeatProducer() {
        RequestContext context = RequestContext.create("testGroup");
        Settings settings = Settings.newBuilder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
            .setClientType(ClientType.PRODUCER)
            .build();

        when(settingManager.getSettings(context)).thenReturn(settings);
        doNothing().when(registerService).registerProducer(any(RequestContext.class), any(Settings.class));

        CompletableFuture<HeartbeatResponse> future = heartbeatService.heartbeat(context, request);
        HeartbeatResponse response = future.join();

        assertNotNull(response);
        assertEquals(Code.OK, response.getStatus().getCode());
        verify(registerService).registerProducer(eq(context), eq(settings));
    }

    @Test
    void testHeartbeatSimpleConsumer() {
        RequestContext context = RequestContext.create("testGroup");
        Settings settings = Settings.newBuilder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
            .setClientType(ClientType.SIMPLE_CONSUMER)
            .setGroup(apache.rocketmq.v2.Resource.newBuilder().setName("testGroup").build())
            .build();

        when(settingManager.getSettings(context)).thenReturn(settings);
        when(registerService.registerConsumer(any(), anyString(), any(), any(), anyBoolean())).thenReturn(mockChannel);

        CompletableFuture<HeartbeatResponse> future = heartbeatService.heartbeat(context, request);
        HeartbeatResponse response = future.join();

        assertNotNull(response);
        assertEquals(Code.OK, response.getStatus().getCode());
        verify(registerService).registerConsumer(eq(context), eq("testGroup"), eq(ClientType.SIMPLE_CONSUMER), eq(settings), eq(false));
    }

    @Test
    void testHeartbeatPushConsumer() {
        RequestContext context = RequestContext.create("testGroup");
        Settings settings = Settings.newBuilder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder()
            .setClientType(ClientType.PUSH_CONSUMER)
            .setGroup(apache.rocketmq.v2.Resource.newBuilder().setName("testGroup").build())
            .build();

        when(settingManager.getSettings(context)).thenReturn(settings);
        when(registerService.registerConsumer(any(), anyString(), any(), any(), anyBoolean())).thenReturn(mockChannel);

        CompletableFuture<HeartbeatResponse> future = heartbeatService.heartbeat(context, request);
        HeartbeatResponse response = future.join();

        assertNotNull(response);
        assertEquals(Code.OK, response.getStatus().getCode());
        verify(registerService).registerConsumer(eq(context), eq("testGroup"), eq(ClientType.PUSH_CONSUMER), eq(settings), eq(false));
    }

    @Test
    void testConstructor() {
        assertNotNull(heartbeatService);
    }
}
