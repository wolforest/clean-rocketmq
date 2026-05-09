package cn.coderule.wolfmq.rpc.common.core.relay.request;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumerRequestTest {

    @Test
    void testDefaultConstructor() {
        ConsumerRequest request = new ConsumerRequest();
        
        assertNotNull(request);
        assertNull(request.getContext());
    }

    @Test
    void testSettersAndGetters() {
        ConsumerRequest request = new ConsumerRequest();
        RequestContext context = mock(RequestContext.class);
        
        request.setContext(context);
        
        assertSame(context, request.getContext());
    }

    @Test
    void testSerializable() {
        ConsumerRequest request = new ConsumerRequest();
        
        assertTrue(request instanceof java.io.Serializable);
    }
}
