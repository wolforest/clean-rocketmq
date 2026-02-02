package cn.coderule.minimq.hello.middleware.rabbitmq;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class RabbitConnection {
    private static final int DEFAULT_PORT = 5672;

    private final ConnectionFactory factory;
    private final ScheduledExecutorService scheduler;

    private Connection connection = null;
    private Channel channel = null;

    public RabbitConnection() {
        this.factory = new ConnectionFactory();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
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

    public void close(int seconds) {
        scheduler.schedule(
            () -> close(),
            seconds,
            TimeUnit.SECONDS
        );
        scheduler.shutdown();
    }

    public void close() {
        try {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }
            if (this.connection != null && this.connection.isOpen()) {
                this.connection.close();
            }
            log.info("rabbitmq channel and connection were closed");
        } catch (Exception e) {
            log.error("close error: {}", e.getMessage(), e);
        }
    }
}
