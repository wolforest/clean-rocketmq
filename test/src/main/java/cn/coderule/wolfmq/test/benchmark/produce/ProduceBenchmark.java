package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.Config;
import cn.coderule.wolfmq.test.benchmark.core.Report;
import cn.coderule.wolfmq.test.benchmark.utils.MessageUtils;
import cn.coderule.wolfmq.test.benchmark.utils.TopicUtils;
import cn.coderule.wolfmq.test.manager.ProducerManager;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;

@Slf4j
public class ProduceBenchmark implements Benchmark {
    private Config config;
    private TopicUtils topicUtils;
    private Producer producer;
    private Report report;

    @Override
    public void prepare(Config config) {
        this.config = config;
        this.topicUtils = new TopicUtils(config.getTopicNumber());
        topicUtils.createTopicList();

        this.producer = ProducerManager.buildProducer(
            topicUtils.getTopicList()
        );
        this.report = new Report();
    }

    @Override
    public void benchmark() {
        report.start();

        for (int i = 0; i < config.getRequestNumber(); i++) {
            try {
                Message message = MessageUtils.createMessage(
                    topicUtils.getRandomTopic(),
                    config.getMessageSize()
                );

                producer.send(message);
                report.increaseSuccessCount();
            } catch (Throwable t) {
                report.increaseFailureCount();
                log.error("Failed to send message: ", t);
            }
        }

        report.stop();
    }

    @Override
    public void cleanup() {
        try {
            stopProducer();
            topicUtils.deleteTopicList();
        } catch (IOException e) {
            log.error("cleanup exception: ", e);
        }
    }

    private void stopProducer() throws IOException {
        if (producer == null) {
            return;
        }

        producer.close();
    }

    @Override
    public Report getReport() {
        return report;
    }
}
