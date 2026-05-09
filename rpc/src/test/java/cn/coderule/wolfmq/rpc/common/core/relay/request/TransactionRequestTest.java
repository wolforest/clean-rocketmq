package cn.coderule.wolfmq.rpc.common.core.relay.request;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionRequestTest {

    @Test
    void testDefaultConstructor() {
        TransactionRequest request = new TransactionRequest();
        
        assertNotNull(request);
        assertNull(request.getContext());
        assertNull(request.getMessageBO());
    }

    @Test
    void testBuild() {
        MessageBO messageBO = mock(MessageBO.class);
        
        TransactionRequest request = TransactionRequest.build(messageBO);
        
        assertNotNull(request);
        assertNotNull(request.getContext());
        assertSame(messageBO, request.getMessageBO());
    }

    @Test
    void testSettersAndGetters() {
        TransactionRequest request = new TransactionRequest();
        MessageBO messageBO = mock(MessageBO.class);
        
        request.setMessageBO(messageBO);
        
        assertSame(messageBO, request.getMessageBO());
    }

    @Test
    void testSerializable() {
        TransactionRequest request = new TransactionRequest();
        
        assertTrue(request instanceof java.io.Serializable);
    }
}
