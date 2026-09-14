package cn.coderule.wolfmq.rpc.common.grpc.interceptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionInterceptorTest {

    @Test
    void globalExceptionInterceptor_ShouldBeInstantiable() {
        GlobalExceptionInterceptor interceptor = new GlobalExceptionInterceptor();
        assertNotNull(interceptor);
    }
}