
package cn.coderule.minimq.rpc.common.rpc.config;

public class RpcSystemConfig {
    public static final String COM_ROCKETMQ_REMOTING_NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE =
        "com.rocketmq.remoting.nettyPooledByteBufAllocatorEnable";
    public static final String COM_ROCKETMQ_REMOTING_SOCKET_SNDBUF_SIZE =
        "com.rocketmq.remoting.socket.sndbuf.size";
    public static final String COM_ROCKETMQ_REMOTING_SOCKET_RCVBUF_SIZE =
        "com.rocketmq.remoting.socket.rcvbuf.size";
    public static final String COM_ROCKETMQ_REMOTING_SOCKET_BACKLOG =
        "com.rocketmq.remoting.socket.backlog";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_ASYNC_SEMAPHORE_VALUE =
        "com.rocketmq.remoting.clientAsyncSemaphoreValue";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_ONEWAY_SEMAPHORE_VALUE =
        "com.rocketmq.remoting.clientOnewaySemaphoreValue";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_WORKER_SIZE =
        "com.rocketmq.remoting.client.worker.size";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_CONNECT_TIMEOUT =
        "com.rocketmq.remoting.client.connect.timeout";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_CHANNEL_MAX_IDLE_SECONDS =
        "com.rocketmq.remoting.client.channel.maxIdleTimeSeconds";
    public static final String COM_ROCKETMQ_REMOTING_CLIENT_CLOSE_SOCKET_IF_TIMEOUT =
        "com.rocketmq.remoting.client.closeSocketIfTimeout";
    public static final String COM_ROCKETMQ_REMOTING_WRITE_BUFFER_HIGH_WATER_MARK_VALUE =
        "com.rocketmq.remoting.write.buffer.high.water.mark";
    public static final String COM_ROCKETMQ_REMOTING_WRITE_BUFFER_LOW_WATER_MARK =
        "com.rocketmq.remoting.write.buffer.low.water.mark";

    public static final boolean NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE = //
        Boolean.parseBoolean(System.getProperty(COM_ROCKETMQ_REMOTING_NETTY_POOLED_BYTE_BUF_ALLOCATOR_ENABLE, "false"));
    public static final int CLIENT_ASYNC_SEMAPHORE_VALUE = //
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_ASYNC_SEMAPHORE_VALUE, "65535"));
    public static final int CLIENT_ONEWAY_SEMAPHORE_VALUE =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_ONEWAY_SEMAPHORE_VALUE, "65535"));
    public static int socketSndbufSize =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_SOCKET_SNDBUF_SIZE, "0"));
    public static int socketRcvbufSize =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_SOCKET_RCVBUF_SIZE, "0"));
    public static int socketBacklog =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_SOCKET_BACKLOG, "1024"));
    public static int clientWorkerSize =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_WORKER_SIZE, "4"));
    public static int connectTimeoutMillis =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_CONNECT_TIMEOUT, "3000"));
    public static int clientChannelMaxIdleTimeSeconds =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_CHANNEL_MAX_IDLE_SECONDS, "120"));
    public static boolean clientCloseSocketIfTimeout =
        Boolean.parseBoolean(System.getProperty(COM_ROCKETMQ_REMOTING_CLIENT_CLOSE_SOCKET_IF_TIMEOUT, "true"));
    public static int writeBufferHighWaterMark =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_WRITE_BUFFER_HIGH_WATER_MARK_VALUE, "0"));
    public static int writeBufferLowWaterMark =
        Integer.parseInt(System.getProperty(COM_ROCKETMQ_REMOTING_WRITE_BUFFER_LOW_WATER_MARK, "0"));

}
