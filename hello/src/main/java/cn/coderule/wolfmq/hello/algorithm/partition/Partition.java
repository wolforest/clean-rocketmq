package cn.coderule.wolfmq.hello.algorithm.partition;

import cn.coderule.common.util.lang.string.StringUtil;

public class Partition {
    public static void main(String[] args) {
        testPartitionBySuffix();

    }

    private static void testPartitionBySuffix() {
        System.out.println("abc-a0".charAt(4));
        System.out.println("abc-b0".charAt(4));
        System.out.println("abc-10".charAt(4));

        System.out.println("\n");
        System.out.println("************************************************");
        System.out.println(Integer.parseInt("abc-10",4, 6, 10));
        System.out.println(Integer.parseInt("abc-12",4, 6, 10));
        System.out.println(Integer.parseInt("abc-30",4, 6, 10));
        System.out.println(Integer.parseInt("abc-51",4, 6, 10));
    }

    private static void testStringPartition() {
        System.out.println("\n");
        System.out.println("************************************************");

        System.out.println("abc1".hashCode() % 2);
        System.out.println("abc2".hashCode() % 2);
        System.out.println("abc3".hashCode() % 2);
        System.out.println("abc4".hashCode() % 2);
        System.out.println("abc5".hashCode() % 2);
        System.out.println("abc6".hashCode() % 2);
        System.out.println("abc7".hashCode() % 2);
        System.out.println("abc8".hashCode() % 2);
        System.out.println("abc9".hashCode() % 2);

        System.out.println("\n");
        System.out.println("************************************************");

        for (int i = 0; i < 10; i++) {
            System.out.println(Math.abs(StringUtil.uuid().hashCode()) % 2);
        }
    }

    private static void testNumberPartition() {
        System.out.println(99 % 2);
        System.out.println(99 % 3);
        System.out.println(99 % 4);
        System.out.println(99 % 5);
        System.out.println(99 % 6);
        System.out.println(99 % 7);
        System.out.println(99 % 8);
        System.out.println(99 % 9);
        System.out.println(99 % 10);

        System.out.println("\n");
        System.out.println("************************************************");

        System.out.println(12399 % 2);
        System.out.println(12399 % 3);
        System.out.println(12399 % 4);
        System.out.println(12399 % 5);
        System.out.println(12399 % 6);
        System.out.println(12399 % 7);
        System.out.println(12399 % 8);
        System.out.println(12399 % 9);
        System.out.println(12399 % 10);

        System.out.println("\n");
        System.out.println("************************************************");

        System.out.println(12399 % 100 % 2);
        System.out.println(12399 % 100 % 3);
        System.out.println(12399 % 100 % 4);
        System.out.println(12399 % 100 % 5);
        System.out.println(12399 % 100 % 6);
        System.out.println(12399 % 100 % 7);
        System.out.println(12399 % 100 % 8);
        System.out.println(12399 % 100 % 9);
        System.out.println(12399 % 100 % 10);
    }
}
