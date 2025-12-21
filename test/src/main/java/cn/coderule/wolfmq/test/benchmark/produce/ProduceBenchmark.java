package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.benchmark.core.Benchmark;
import cn.coderule.wolfmq.test.benchmark.core.Config;
import cn.coderule.wolfmq.test.benchmark.core.Report;
import cn.coderule.wolfmq.test.benchmark.utils.TopicUtils;
import org.apache.rocketmq.client.apis.producer.Producer;

public class ProduceBenchmark implements Benchmark {
    private TopicUtils topicUtils;
    private Producer producer;

    @Override
    public void prepare(Config config) {
        this.topicUtils = new TopicUtils(config.getTopicNumber());

        topicUtils.createTopicList();
    }

    @Override
    public void benchmark() {

    }

    @Override
    public void cleanup() {
        topicUtils.deleteTopicList();
    }

    @Override
    public Report getReport() {
        return null;
    }
}
