package cn.coderule.minimq.hello.middleware.rabbitmq;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ConnectionManager {
    private static final int DEFAULT_PORT = 5672;

    private ConnectionFactory factory;
    private Connection connection = null;
    private Channel channel = null;

    public ConnectionManager() {
        this.factory = new ConnectionFactory();
    }

    public Channel connect(String host) {
        return connect(host, DEFAULT_PORT);
    }

    public Channel connect(String host, int port) {
        try {
            this.factory.setHost(host);
            this.factory.setPort(port);

            this.connection = this.factory.newConnection();
            this.channel = this.connection.createChannel();
            return this.channel;
        } catch (Exception e) {
            log.error("connect error: {}", e.getMessage(), e);
        }

        return this.channel;
    }
}
