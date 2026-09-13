package cn.coderule.wolfmq.rpc.common.grpc.interceptor;

import cn.coderule.wolfmq.rpc.common.grpc.core.constants.GrpcConstants;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextInterceptorTest {

    @Test
    void contextInterceptor_ShouldBeInstantiable() {
        ContextInterceptor interceptor = new ContextInterceptor();
        assertNotNull(interceptor);
    }
}