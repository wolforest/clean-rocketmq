package cn.coderule.wolfmq.rpc.common.core.relay.request;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumeRequestTest {

    @Test
    void testDefaultConstructor() {
        ConsumeRequest request = new ConsumeRequest();
        
        assertNotNull(request);
        assertNull(request.getContext());
    }

    @Test
    void testSettersAndGetters() {
        ConsumeRequest request = new ConsumeRequest();
        RequestContext context = mock(RequestContext.class);
        
        request.setContext(context);
        
        assertSame(context, request.getContext());
    }

    @Test
    void testSerializable() {
        ConsumeRequest request = new ConsumeRequest();
        
        assertTrue(request instanceof java.io.Serializable);
    }
}
