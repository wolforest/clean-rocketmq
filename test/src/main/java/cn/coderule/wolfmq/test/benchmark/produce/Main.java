package cn.coderule.wolfmq.test.benchmark.produce;

public class Main {
    public static void main(String[] args) {
        ProduceBenchmarkSuit suit = new ProduceBenchmarkSuit();
        suit.initConfig();

        suit.benchmark();

        suit.showReport();
    }
}
