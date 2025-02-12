package cn.coderule.minimq.hello.rocketmq;

import cn.coderule.common.util.lang.ThreadUtil;
import java.io.IOException;
import java.util.Collections;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;

public class HelloConsumer {
    private static final ClientServiceProvider provider = ClientServiceProvider.loadService();
    private static final String TOPIC = "TestTopic";
    private static final String ENDPOINT = "localhost:8081";
    private static final String GROUP = "TestGroup";

    public static void main(String[] args) throws Exception {
        new HelloConsumer().consume();
    }

    public void consume() throws ClientException, IOException {
        ClientConfiguration configuration = ClientConfiguration.newBuilder()
            .enableSsl(false)
            .setEndpoints(ENDPOINT)
            .build();

        String tag = "*";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);

        PushConsumer pushConsumer =provider.newPushConsumerBuilder()
            .setClientConfiguration(configuration)
            .setConsumerGroup(GROUP)
            .setSubscriptionExpressions(Collections.singletonMap(TOPIC, filterExpression))
            .setMessageListener(messageView -> {
                System.out.println("Receive message, msgId: " + messageView.getMessageId()
                        + ", body: " + messageView.getBody());
                return ConsumeResult.SUCCESS;
            })
            .build();

        ThreadUtil.sleep(100_000);
        pushConsumer.close();
    }
}
