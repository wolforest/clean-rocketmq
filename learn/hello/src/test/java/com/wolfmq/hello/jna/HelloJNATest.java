package com.wolfmq.hello.jna;

import com.wolfmq.hello.java.jna.HelloJNA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloJNATest {

    @Test
    public void test_abs() {
        HelloJNA helloJNA = new HelloJNA();
        assertEquals(5, helloJNA.abs(-5), "jna abs failed");
    }
}
