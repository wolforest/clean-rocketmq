package cn.coderule.wolfmq.test.benchmark.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ConfigBuilder implements Serializable {
    private List<Integer> concurrencyList = new ArrayList<>();
    private int requestNumber = 10000;
    private int topicNumber = 10;
    private int groupNumber = 10;

    public void concurrencyList(List<Integer> concurrencyList) {
        this.concurrencyList = concurrencyList;
    }

    public void requestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    public void topicNumber(int topicNumber) {
        this.topicNumber = topicNumber;
    }

    public void groupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public List<Config> build() {
        List<Config> configList = new ArrayList<>();

        for (int concurrency : concurrencyList) {
            Config config = Config.builder()
                .concurrency(concurrency)
                .requestNumber(requestNumber)
                .topicNumber(topicNumber)
                .groupNumber(groupNumber)
                .build();

            configList.add(config);
        }

        return configList;
    }
}
