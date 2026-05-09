package cn.coderule.wolfmq.rpc.common.core.channel;

import cn.coderule.wolfmq.rpc.common.core.channel.remote.RemoteChannel;
import cn.coderule.wolfmq.rpc.common.grpc.channel.GrpcChannel;
import cn.coderule.wolfmq.rpc.common.rpc.channel.RpcChannel;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ChannelHelperTest {

    @Test
    void testIsRemoteWithRemoteChannel() {
        RemoteChannel remoteChannel = mock(RemoteChannel.class);
        
        assertTrue(ChannelHelper.isRemote(remoteChannel));
    }

    @Test
    void testIsRemoteWithNonRemoteChannel() {
        Channel channel = mock(Channel.class);
        
        assertFalse(ChannelHelper.isRemote(channel));
    }

    @Test
    void testGetChannelProtocolTypeWithGrpcChannel() {
        GrpcChannel grpcChannel = mock(GrpcChannel.class);
        
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(grpcChannel);
        
        assertEquals(ChannelProtocolType.GRPC_V2, type);
    }

    @Test
    void testGetChannelProtocolTypeWithRpcChannel() {
        RpcChannel rpcChannel = mock(RpcChannel.class);
        
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(rpcChannel);
        
        assertEquals(ChannelProtocolType.REMOTING, type);
    }

    @Test
    void testGetChannelProtocolTypeWithRemoteChannel() {
        RemoteChannel remoteChannel = mock(RemoteChannel.class);
        when(remoteChannel.getType()).thenReturn(ChannelProtocolType.GRPC_V2);
        
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(remoteChannel);
        
        assertEquals(ChannelProtocolType.GRPC_V2, type);
    }

    @Test
    void testGetChannelProtocolTypeWithUnknownChannel() {
        Channel channel = mock(Channel.class);
        
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(channel);
        
        assertEquals(ChannelProtocolType.UNKNOWN, type);
    }

    @Test
    void testGetChannelProtocolTypeWithEmbeddedChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        
        ChannelProtocolType type = ChannelHelper.getChannelProtocolType(channel);
        
        assertEquals(ChannelProtocolType.UNKNOWN, type);
    }
}
