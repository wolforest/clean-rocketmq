package cn.coderule.minimq.hello.rocketmq;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

@Slf4j
public class HelloProducer {
    private static final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private static final String TOPIC = "TestTopic";
    private static final String ENDPOINT = "127.0.0.1:8081";
    public static void main(String[] args) throws ClientException, IOException {
        new HelloProducer().sendMessage();
    }

    public void sendMessage() throws ClientException, IOException {
        Producer producer = buildProducer();
        Message message = buildMessage();

        try {
            SendReceipt sendReceipt = producer.send(message);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (ClientException e) {
            log.error("Failed to send message", e);
        }

        producer.close();
    }

    private Message buildMessage() {
        return provider.newMessageBuilder()
            .setTopic(TOPIC)
            .setKeys("messageKey")
            .setTag("messageTag")
            .setBody("messageBody".getBytes())
            .build();
    }

    private Producer buildProducer() throws ClientException {
        ClientConfiguration configuration = ClientConfiguration.newBuilder()
            .enableSsl(false)
            .setEndpoints(ENDPOINT)
            .build();

        return provider.newProducerBuilder()
            .setTopics(TOPIC)
            .setClientConfiguration(configuration)
            .build();
    }
}
