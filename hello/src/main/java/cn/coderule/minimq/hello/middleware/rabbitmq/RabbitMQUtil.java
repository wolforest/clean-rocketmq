package cn.coderule.minimq.hello.middleware.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ工具类，提供常用的连接和队列操作
 */
public class RabbitMQUtil {

    /**
     * 创建持久化队列
     */
    public static AMQP.Queue.DeclareOk createDurableQueue(Channel channel, String queueName) throws IOException {
        return channel.queueDeclare(queueName, true, false, false, null);
    }

    /**
     * 创建优先级队列
     */
    public static AMQP.Queue.DeclareOk createPriorityQueue(Channel channel, String queueName, int maxPriority) throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("x-max-priority", maxPriority);
        return channel.queueDeclare(queueName, true, false, false, args);
    }

    /**
     * 创建死信队列绑定
     */
    public static void bindDeadLetterQueue(Channel channel, String queueName, String deadLetterExchange, String deadLetterRoutingKey) throws IOException {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", deadLetterExchange);
        args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        channel.queueDeclare(queueName, true, false, false, args);
    }

    /**
     * 创建持久化消息属性
     */
    public static AMQP.BasicProperties createDurableProperties() {
        return new AMQP.BasicProperties.Builder()
                .deliveryMode(2)
                .build();
    }

    /**
     * 创建TTL消息属性
     */
    public static AMQP.BasicProperties createTTLProperties(String ttlMs) {
        return new AMQP.BasicProperties.Builder()
                .expiration(ttlMs)
                .build();
    }

    /**
     * 创建优先级消息属性
     */
    public static AMQP.BasicProperties createPriorityProperties(int priority) {
        return new AMQP.BasicProperties.Builder()
                .priority(priority)
                .build();
    }

    /**
     * 安全关闭连接
     */
    public static void closeConnectionSafely(Connection connection) {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (Exception e) {
                System.err.println("关闭连接时发生异常: " + e.getMessage());
            }
        }
    }

    /**
     * 安全关闭通道
     */
    public static void closeChannelSafely(Channel channel) {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
            } catch (Exception e) {
                System.err.println("关闭通道时发生异常: " + e.getMessage());
            }
        }
    }

    /**
     * 检查连接状态
     */
    public static boolean isConnectionHealthy(Connection connection) {
        return connection != null && connection.isOpen();
    }

    /**
     * 检查通道状态
     */
    public static boolean isChannelHealthy(Channel channel) {
        return channel != null && channel.isOpen();
    }

    /**
     * 创建消费者标签
     */
    public static String createConsumerTag(String prefix, int id) {
        return prefix + "-" + id;
    }

    /**
     * 等待确认并处理超时
     */
    public static boolean waitForConfirmWithTimeout(Channel channel, long timeoutMs) {
        try {
            return channel.waitForConfirms(timeoutMs);
        } catch (Exception e) {
            System.err.println("等待确认超时: " + e.getMessage());
            return false;
        }
    }

    /**
     * 重试机制配置
     */
    public static class RetryConfig {
        public final int maxRetries;
        public final long initialDelayMs;
        public final double backoffMultiplier;
        public final long maxDelayMs;

        public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier, long maxDelayMs) {
            this.maxRetries = maxRetries;
            this.initialDelayMs = initialDelayMs;
            this.backoffMultiplier = backoffMultiplier;
            this.maxDelayMs = maxDelayMs;
        }

        public static RetryConfig getDefault() {
            return new RetryConfig(3, 1000, 2.0, 10000);
        }

        public static RetryConfig getAggressive() {
            return new RetryConfig(5, 500, 1.5, 5000);
        }
    }

    /**
     * 执行带退避的重试
     */
    public static boolean executeWithBackoff(Runnable operation, RetryConfig config) {
        int attempt = 0;
        long delay = config.initialDelayMs;

        while (attempt <= config.maxRetries) {
            try {
                operation.run();
                return true;
            } catch (Exception e) {
                attempt++;
                if (attempt > config.maxRetries) {
                    System.err.println("重试失败，已达最大重试次数: " + e.getMessage());
                    return false;
                }

                System.err.println("操作失败，" + delay + "ms后进行第" + attempt + "次重试: " + e.getMessage());

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }

                delay = (long) Math.min(delay * config.backoffMultiplier, config.maxDelayMs);
            }
        }
        return false;
    }
}
