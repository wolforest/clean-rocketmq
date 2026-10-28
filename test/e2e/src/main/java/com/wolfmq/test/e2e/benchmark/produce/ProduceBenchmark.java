package com.wolfmq.test.e2e.benchmark.produce;

import cn.coderule.common.util.lang.ThreadUtil;
import com.wolfmq.test.e2e.benchmark.core.Benchmark;
import com.wolfmq.test.e2e.benchmark.core.Config;
import com.wolfmq.test.e2e.benchmark.core.Report;
import com.wolfmq.test.e2e.benchmark.utils.MessageUtils;
import com.wolfmq.test.e2e.benchmark.utils.TopicUtils;
import com.wolfmq.test.e2e.manager.ProducerManager;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;

@Slf4j
public class ProduceBenchmark implements Benchmark {
    private Config config;
    private TopicUtils topicUtils;
    private Producer producer;
    private Report report;
    private CountDownLatch startLatch;

    @Override
    public void prepare(Config config) {
        this.config = config;
        this.topicUtils = new TopicUtils(config.getTopicNumber());
        topicUtils.createTopicList();

        ThreadUtil.sleep(3000);

        this.producer = ProducerManager.buildProducer(
            topicUtils.getTopicList()
        );

        this.report = new Report();
    }

    @Override
    public void benchmark() {
        if (startLatch != null) {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        report.start();

        int requestsPerThread = config.getRequestNumber() / config.getConcurrency();
        for (int i = 0; i < requestsPerThread; i++) {
            sendMessage();
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

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public void setStartLatch(CountDownLatch latch) {
        this.startLatch = latch;
    }

    private void sendMessage() {
        try {
            Message message = MessageUtils.createMessage(
                topicUtils.getRandomTopic(),
                config.getMessageSize()
            );

            if (null == producer) {
                log.error("producer is null, msg={}", message);
            }

            producer.send(message);
            report.increaseSuccessCount();
        } catch (Throwable t) {
            report.increaseFailureCount();
            log.error("Failed to send message: ", t);
        }
    }

    private void stopProducer() throws IOException {
        if (producer == null) {
            return;
        }

        producer.close();
    }

}
