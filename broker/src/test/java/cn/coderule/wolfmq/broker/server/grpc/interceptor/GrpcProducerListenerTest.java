package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import cn.coderule.wolfmq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.wolfmq.domain.core.enums.produce.ProducerEvent;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import io.netty.channel.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcProducerListenerTest {

    @Mock
    private SettingManager settingManager;

    @Mock
    private ChannelManager channelManager;

    @Mock
    private Channel channel;

    private GrpcProducerListener listener;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        listener = new GrpcProducerListener(settingManager, channelManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void testConstructorNotNull() {
        assertNotNull(listener);
    }

    @Test
    void testHandleUnregisterRemovesChannel() {
        String clientId = "test-producer-1";
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel);

        listener.handle(ProducerEvent.CLIENT_UNREGISTER, "test-group", channelInfo);

        verify(channelManager).removeChannel(channelInfo.getClientId());
        verify(settingManager).removeSettings(channelInfo.getClientId());
    }

    @Test
    void testHandleRegisterDoesNothing() {
        ClientChannelInfo channelInfo = new ClientChannelInfo(channel);

        listener.handle(ProducerEvent.GROUP_UNREGISTER, "test-group", channelInfo);

        verifyNoInteractions(channelManager);
        verifyNoInteractions(settingManager);
    }
}