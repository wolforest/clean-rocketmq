package cn.coderule.minimq.hello.middleware.rabbitmq;

import com.rabbitmq.client.*;


import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RabbitMQ高级使用场景示例
 */
public class RabbitMQAdvancedScenarios {

    public static void main(String[] args) throws Exception {
        System.out.println("=== RabbitMQ Advanced Scenarios Demo ===");

        // 1. 简单生产者消费者
        simpleProducerConsumer();

        // 2. 批量生产者
        batchProducer();

        // 3. 优先级队列
        priorityQueue();

        // 4. 死信队列
        deadLetterQueue();

        // 5. 持久化队列
        durableQueue();

        // 6. 发布确认模式
        publisherConfirms();

        // 7. 事务模式
        transactionMode();

        // 8. 路由键和交换机
        routingAndExchange();

        // 9. 主题模式
        topicExchange();

        // 10. 延迟消息
        delayedMessage();

        // 11. 消息TTL
        messageTTL();

        // 12. 消费者确认
        consumerAcknowledgement();

        // 13. 多消费者负载均衡
        multipleConsumers();

        // 14. 消息重试机制
        messageRetryMechanism();

        // 15. 限流生产者
        rateLimitedProducer();

        System.out.println("=== All scenarios completed ===");
    }

    /**
     * 场景1: 简单生产者消费者
     */
    private static void simpleProducerConsumer() throws Exception {
        System.out.println("\n--- 场景1: 简单生产者消费者 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明队列
            channel.queueDeclare("simple-queue", false, false, false, null);

            // 生产者线程
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < 5; i++) {
                        String message = "Simple Message " + i;
                        channel.basicPublish("", "simple-queue", null, message.getBytes());
                        System.out.println("[生产者] 发送: " + message);
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    System.err.println("生产者发送失败: " + e.getMessage());
                }
            });

            // 消费者
            DeliverCallback callback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("[消费者] 接收: " + message);
            };

            channel.basicConsume("simple-queue", true, callback, consumerTag -> {});

            producer.start();
            producer.join(6000);

            channel.basicCancel("simple-queue");
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景2: 批量生产者
     */
    private static void batchProducer() throws Exception {
        System.out.println("\n--- 场景2: 批量生产者 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("batch-queue", false, false, false, null);

            // 批量发送消息
            List<String> messages = Arrays.asList(
                "Batch Message 1", "Batch Message 2", "Batch Message 3",
                "Batch Message 4", "Batch Message 5"
            );

            for (String message : messages) {
                channel.basicPublish("", "batch-queue", null, message.getBytes());
                System.out.println("[批量生产者] 发送: " + message);
            }

            Thread.sleep(2000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景3: 优先级队列
     */
    private static void priorityQueue() throws Exception {
        System.out.println("\n--- 场景3: 优先级队列 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明优先级队列
            Map<String, Object> args = new HashMap<>();
            args.put("x-max-priority", 10);
            channel.queueDeclare("priority-queue", true, false, false, args);

            // 发送不同优先级的消息
            for (int i = 0; i < 5; i++) {
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .priority((5 - i)) // 优先级 5,4,3,2,1
                    .build();

                String message = "Priority " + (5 - i) + ": Message " + i;
                channel.basicPublish("", "priority-queue", properties, message.getBytes());
                System.out.println("[优先级生产者] 发送优先级 " + (5 - i) + ": " + message);
            }

            Thread.sleep(2000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景4: 死信队列
     */
    private static void deadLetterQueue() throws Exception {
        System.out.println("\n--- 场景4: 死信队列 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明死信队列
            channel.queueDeclare("dead-letter-queue", true, false, false, null);

            // 声明主队列，绑定死信交换机
            Map<String, Object> args = new HashMap<>();
            args.put("x-dead-letter-exchange", "");
            args.put("x-dead-letter-routing-key", "dead-letter-queue");
            channel.queueDeclare("main-queue-with-dlq", true, false, false, args);

            // 发送一条会过期的消息
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("5000") // 5秒后过期
                    .build();

            String message = "This message will expire and go to DLQ";
            channel.basicPublish("", "main-queue-with-dlq", properties, message.getBytes());
            System.out.println("[死信队列] 发送消息，5秒后进入死信队列");

            Thread.sleep(6000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景5: 持久化队列
     */
    private static void durableQueue() throws Exception {
        System.out.println("\n--- 场景5: 持久化队列 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明持久化队列
            channel.queueDeclare("durable-queue", true, false, false, null);

            // 发送持久化消息
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .deliveryMode(2) // 持久化消息
                    .build();

            String message = "Durable Message";
            channel.basicPublish("", "durable-queue", properties, message.getBytes());
            System.out.println("[持久化队列] 发送持久化消息: " + message);

            Thread.sleep(1000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景6: 发布确认模式
     */
    private static void publisherConfirms() throws Exception {
        System.out.println("\n--- 场景6: 发布确认模式 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("confirm-queue", false, false, false, null);

            // 启用发布确认
            channel.confirmSelect();

            // 异步发送消息
            for (int i = 0; i < 5; i++) {
                String message = "Confirm Message " + i;
                channel.basicPublish("", "confirm-queue", null, message.getBytes());

                // 等待确认
                boolean confirmed = channel.waitForConfirms(5000);
                if (confirmed) {
                    System.out.println("[发布确认] 消息 " + i + " 确认成功");
                } else {
                    System.out.println("[发布确认] 消息 " + i + " 确认超时");
                }
            }

        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景7: 事务模式
     */
    private static void transactionMode() throws Exception {
        System.out.println("\n--- 场景7: 事务模式 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("transaction-queue", false, false, false, null);

            try {
                // 开始事务
                channel.txSelect();

                // 在事务中发送消息
                for (int i = 0; i < 3; i++) {
                    String message = "Transaction Message " + i;
                    channel.basicPublish("", "transaction-queue", null, message.getBytes());
                    System.out.println("[事务] 在事务中发送: " + message);
                }

                // 提交事务
                channel.txCommit();
                System.out.println("[事务] 事务提交成功");

            } catch (Exception e) {
                // 回滚事务
                channel.txRollback();
                System.out.println("[事务] 事务回滚");
            }

            Thread.sleep(1000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景8: 路由键和交换机
     */
    private static void routingAndExchange() throws Exception {
        System.out.println("\n--- 场景8: 路由键和交换机 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明直连交换机
            channel.exchangeDeclare("direct-exchange", BuiltinExchangeType.DIRECT, true);

            // 声明多个队列并绑定
            String[] queueNames = {"queue-A", "queue-B", "queue-C"};
            for (String queueName : queueNames) {
                channel.queueDeclare(queueName, true, false, false, null);
                channel.queueBind(queueName, "direct-exchange", queueName);
            }

            // 使用路由键发送到不同队列
            for (String name : queueNames) {
                String message = "Message for " + name;
                channel.basicPublish("direct-exchange", name, null, message.getBytes());
                System.out.println("[路由] 发送到 " + name + ": " + message);
            }

            Thread.sleep(2000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景9: 主题模式
     */
    private static void topicExchange() throws Exception {
        System.out.println("\n--- 场景9: 主题模式 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");

            // 声明主题交换机
            channel.exchangeDeclare("topic-exchange", BuiltinExchangeType.TOPIC, true);

            // 声明队列并绑定主题模式
            channel.queueDeclare("log-queue", true, false, false, null);
            channel.queueBind("log-queue", "topic-exchange", "logs.*.error");

            // 发送到不同主题
            String[] messages = {
                "Error: System down",
                "Warning: High memory usage",
                "Info: User login"
            };

            String[] routingKeys = {
                "logs.system.error",
                "logs.system.warning",
                "logs.user.info"
            };

            for (int i = 0; i < messages.length; i++) {
                channel.basicPublish("topic-exchange", routingKeys[i], null, messages[i].getBytes());
                System.out.println("[主题] 发送到 " + routingKeys[i] + ": " + messages[i]);
            }

            Thread.sleep(2000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景10: 延迟消息
     */
    private static void delayedMessage() throws Exception {
        System.out.println("\n--- 场景10: 延迟消息 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("delayed-queue", false, false, false, null);

            // 使用TTL实现延迟
            for (int i = 1; i <= 3; i++) {
                int delaySeconds = i * 2; // 2, 4, 6秒延迟

                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration(String.valueOf(delaySeconds * 1000))
                        .build();

                String message = "Delayed " + delaySeconds + "s: Message " + i;
                channel.basicPublish("", "delayed-queue", properties, message.getBytes());
                System.out.println("[延迟] 发送 " + delaySeconds + "s 后执行: " + message);
            }

            // 等待所有消息执行
            Thread.sleep(8000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景11: 消息TTL
     */
    private static void messageTTL() throws Exception {
        System.out.println("\n--- 场景11: 消息TTL ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("ttl-queue", false, false, false, null);

            // 发送不同TTL的消息
            String[] messages = {"1秒过期", "5秒过期", "10秒过期"};
            String[] ttls = {"1000", "5000", "10000"};

            for (int i = 0; i < messages.length; i++) {
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                        .expiration(ttls[i])
                        .build();

                channel.basicPublish("", "ttl-queue", properties, messages[i].getBytes());
                System.out.println("[TTL] 发送 " + ttls[i] + "ms后过期: " + messages[i]);
            }

            Thread.sleep(2000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景12: 消费者确认
     */
    private static void consumerAcknowledgement() throws Exception {
        System.out.println("\n--- 场景12: 消费者确认 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("ack-queue", false, false, false, null);

            // 手动确认模式的消费者
            DeliverCallback callback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    // 模拟消息处理
                    Thread.sleep(100);
                    System.out.println("[确认消费者] 处理消息: " + message);

                    // 手动确认
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println("[确认消费者] 消息已确认");

                } catch (Exception e) {
                    // 拒绝消息
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    System.out.println("[确认消费者] 消息已拒绝并重新入队");
                }
            };

            channel.basicConsume("ack-queue", false, callback, consumerTag -> {});

            // 发送消息测试
            for (int i = 0; i < 3; i++) {
                String message = "Ack Test Message " + i;
                channel.basicPublish("", "ack-queue", null, message.getBytes());
                Thread.sleep(500);
            }

            Thread.sleep(3000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景13: 多消费者负载均衡
     */
    private static void multipleConsumers() throws Exception {
        System.out.println("\n--- 场景13: 多消费者负载均衡 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("load-balance-queue", false, false, false, null);

            AtomicInteger receivedCount = new AtomicInteger(0);

            // 启动多个消费者
            DeliverCallback callback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                int count = receivedCount.incrementAndGet();
                System.out.println("[多消费者 " + consumerTag.substring(-1) + "] 接收到第" + count + "条消息: " + message);

                // 模拟不同的处理时间
                try {
                    Thread.sleep(100 + (Integer.parseInt(consumerTag.substring(-1)) * 50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            // 启动3个消费者
            String[] consumerTags = new String[3];
            for (int i = 0; i < 3; i++) {
                consumerTags[i] = channel.basicConsume("load-balance-queue", false, callback, consumerTag -> {});
            }

            // 发送10条消息
            for (int i = 0; i < 10; i++) {
                String message = "Load Balance Message " + i;
                channel.basicPublish("", "load-balance-queue", null, message.getBytes());
            }

            Thread.sleep(5000);

            // 取消消费者
            for (String tag : consumerTags) {
                channel.basicCancel(tag);
            }

        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景14: 消息重试机制
     */
    private static void messageRetryMechanism() throws Exception {
        System.out.println("\n--- 场景14: 消息重试机制 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("retry-queue", false, false, false, null);

            // 消费者带重试逻辑
            DeliverCallback callback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    // 模拟处理，70%失败率
                    if (Math.random() > 0.3) {
                        System.out.println("[重试消费者] 成功处理: " + message);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } else {
                        System.out.println("[重试消费者] 处理失败，拒绝消息: " + message);
                        // 拒绝并重新入队进行重试
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                } catch (Exception e) {
                    System.out.println("[重试消费者] 处理异常: " + e.getMessage());
                }
            };

            channel.basicConsume("retry-queue", false, callback, consumerTag -> {});

            // 发送消息
            for (int i = 0; i < 10; i++) {
                String message = "Retry Message " + i;
                channel.basicPublish("", "retry-queue", null, message.getBytes());
                Thread.sleep(200);
            }

            Thread.sleep(4000);
        } finally {
            connection.close(1);
        }
    }

    /**
     * 场景15: 限流生产者
     */
    private static void rateLimitedProducer() throws Exception {
        System.out.println("\n--- 场景15: 限流生产者 ---");

        RabbitConnection connection = new RabbitConnection();
        try {
            Channel channel = connection.connect("127.0.0.1");
            channel.queueDeclare("rate-limited-queue", false, false, false, null);

            // 限流生产者 - 每秒最多发送2条消息
            for (int i = 0; i < 10; i++) {
                String message = "Rate Limited Message " + i;
                channel.basicPublish("", "rate-limited-queue", null, message.getBytes());
                System.out.println("[限流生产者] 发送: " + message);

                // 控制发送速率
                Thread.sleep(500);
            }

        } finally {
            connection.close(1);
        }
    }
}
