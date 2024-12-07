package com.wolf.minimq.hello.atomic;

import java.util.concurrent.atomic.AtomicInteger;

public class HelloAtomic {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        atomicInteger.addAndGet(10);

        System.out.println("atomicInteger.get() = " + atomicInteger.get());
    }
}
