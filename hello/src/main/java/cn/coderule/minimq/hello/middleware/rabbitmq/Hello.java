package cn.coderule.minimq.hello.middleware.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class Hello {
    public static void main(String[] args) throws Exception {
        produce();
        consume();
    }

    private static void produce() throws Exception {
        ConnectionManager manager = new ConnectionManager();

        try {
            Channel channel = manager.connect("127.0.0.1");
            channel.queueDeclare("hello", false, false, false, null);
            String message = "Hello World!";
            channel.basicPublish("", "hello", null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } finally {
            manager.close();
        }
    }

    private static void consume() throws Exception {
        ConnectionManager manager = new ConnectionManager();

        Channel channel = manager.connect("127.0.0.1");
        channel.queueDeclare("hello", false, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume("hello", true, deliverCallback, consumerTag -> { });
        manager.close(10);
    }
}
