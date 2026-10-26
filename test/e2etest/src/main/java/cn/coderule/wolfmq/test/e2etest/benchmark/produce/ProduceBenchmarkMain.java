package cn.coderule.wolfmq.test.e2etest.benchmark.produce;

import cn.coderule.wolfmq.test.e2etest.manager.ClientManager;
import cn.coderule.wolfmq.test.e2etest.manager.ConfigManager;

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
