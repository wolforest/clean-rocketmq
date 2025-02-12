package cn.coderule.minimq.hello.jna;

import cn.coderule.minimq.hello.java.jna.HelloJNA;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HelloJNATest {

    @Test
    public void test_abs() {
        HelloJNA helloJNA = new HelloJNA();
        assertEquals("jna abs failed", 5, helloJNA.abs(-5));
    }
}
