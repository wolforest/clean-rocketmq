package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import apache.rocketmq.v2.Settings;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumerEvent;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.rpc.common.core.channel.ChannelHelper;
import cn.coderule.wolfmq.rpc.common.grpc.channel.GrpcChannel;
import io.netty.channel.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class GrpcConsumerListenerTest {

    @Mock
    private SettingManager settingManager;

    @Mock
    private ChannelManager channelManager;

    @Mock
    private Channel channel;

    private GrpcConsumerListener listener;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        listener = new GrpcConsumerListener(settingManager, channelManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // ==================== REGISTER event tests ====================

    @Test
    void handleRegisterEvent_withValidArgs_callsUpdateSettings() {
        String clientId = "test-client-1";
        String group = "test-group";
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel, clientId, null, 1);
        Settings settings = Settings.newBuilder().build();

        try (MockedStatic<ChannelHelper> channelHelperMock = mockStatic(ChannelHelper.class);
             MockedStatic<GrpcChannel> grpcChannelMock = mockStatic(GrpcChannel.class)) {

            channelHelperMock.when(() -> ChannelHelper.isRemote(channel)).thenReturn(true);
            grpcChannelMock.when(() -> GrpcChannel.parseChannelExtendAttribute(channel)).thenReturn(settings);

            listener.handle(ConsumerEvent.REGISTER, group, new Object(), channelInfo);

            verify(settingManager).updateSettings(clientId, settings);
        }
    }

    @Test
    void handleRegisterEvent_withEmptyArgs_doesNothing() {
        listener.handle(ConsumerEvent.REGISTER, "test-group");

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handleRegisterEvent_withArgs1NotClientChannelInfo_doesNothing() {
        listener.handle(ConsumerEvent.REGISTER, "test-group", new Object(), "not-a-channel-info");

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handleRegisterEvent_withNonRemoteChannel_doesNothing() {
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel, "client-1", null, 1);

        try (MockedStatic<ChannelHelper> channelHelperMock = mockStatic(ChannelHelper.class)) {
            channelHelperMock.when(() -> ChannelHelper.isRemote(channel)).thenReturn(false);

            listener.handle(ConsumerEvent.REGISTER, "test-group", new Object(), channelInfo);

            verify(settingManager, never()).updateSettings(anyString(), any());
        }
    }

    @Test
    void handleRegisterEvent_withNullSettings_doesNotUpdate() {
        String clientId = "test-client-1";
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel, clientId, null, 1);

        try (MockedStatic<ChannelHelper> channelHelperMock = mockStatic(ChannelHelper.class);
             MockedStatic<GrpcChannel> grpcChannelMock = mockStatic(GrpcChannel.class)) {

            channelHelperMock.when(() -> ChannelHelper.isRemote(channel)).thenReturn(true);
            grpcChannelMock.when(() -> GrpcChannel.parseChannelExtendAttribute(channel)).thenReturn(null);

            listener.handle(ConsumerEvent.REGISTER, "test-group", new Object(), channelInfo);

            verify(settingManager, never()).updateSettings(anyString(), any());
        }
    }

    // ==================== UNREGISTER event tests ====================

    @Test
    void handleUnregisterEvent_withValidArgs_callsRemoveChannel() {
        String clientId = "test-client-1";
        String group = "test-group";
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel, clientId, null, 1);

        try (MockedStatic<ChannelHelper> channelHelperMock = mockStatic(ChannelHelper.class)) {
            channelHelperMock.when(() -> ChannelHelper.isRemote(channel)).thenReturn(false);

            listener.handle(ConsumerEvent.UNREGISTER, group, channelInfo);

            verify(channelManager).removeChannel(clientId);
        }
    }

    @Test
    void handleUnregisterEvent_withEmptyArgs_doesNothing() {
        listener.handle(ConsumerEvent.UNREGISTER, "test-group");

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handleUnregisterEvent_withArgs0NotClientChannelInfo_doesNothing() {
        listener.handle(ConsumerEvent.UNREGISTER, "test-group", "not-a-channel-info");

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handleUnregisterEvent_withRemoteChannel_doesNotRemove() {
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel, "client-1", null, 1);

        try (MockedStatic<ChannelHelper> channelHelperMock = mockStatic(ChannelHelper.class)) {
            channelHelperMock.when(() -> ChannelHelper.isRemote(channel)).thenReturn(true);

            listener.handle(ConsumerEvent.UNREGISTER, "test-group", channelInfo);

            verify(channelManager, never()).removeChannel(anyString());
        }
    }

    // ==================== Unknown event tests ====================

    @Test
    void handle_withUnknownEvent_doesNothing() {
        listener.handle(ConsumerEvent.CHANGE, "test-group", new Object());

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handle_withClientRegisterEvent_doesNothing() {
        listener.handle(ConsumerEvent.CLIENT_REGISTER, "test-group", new Object());

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }

    @Test
    void handle_withClientUnregisterEvent_doesNothing() {
        listener.handle(ConsumerEvent.CLIENT_UNREGISTER, "test-group", new Object());

        verifyNoInteractions(settingManager);
        verifyNoInteractions(channelManager);
    }
}
