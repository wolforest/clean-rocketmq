package cn.coderule.wolfmq.rpc.common.grpc.interceptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeaderInterceptorTest {

    @Test
    void headerInterceptor_ShouldBeInstantiable() {
        HeaderInterceptor interceptor = new HeaderInterceptor();
        assertNotNull(interceptor);
    }
}