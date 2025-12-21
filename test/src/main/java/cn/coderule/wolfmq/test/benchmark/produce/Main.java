package cn.coderule.wolfmq.test.benchmark.produce;

import cn.coderule.wolfmq.test.manager.ClientManager;
import cn.coderule.wolfmq.test.manager.ConfigManager;

public class Main {
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
