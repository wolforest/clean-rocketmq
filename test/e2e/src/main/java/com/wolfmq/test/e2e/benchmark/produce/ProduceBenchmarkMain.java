package com.wolfmq.test.e2e.benchmark.produce;

import com.wolfmq.test.e2e.manager.ClientManager;
import com.wolfmq.test.e2e.manager.ConfigManager;

public class ProduceBenchmarkMain {
    public static void main(String[] args) throws Exception {
        ConfigManager.init();
        ClientManager.start();

        ProduceBenchmarkSuit suit = new ProduceBenchmarkSuit();
        suit.initConfig();
        suit.benchmark();
        suit.showReport();

        ClientManager.shutdown();
    }
}
