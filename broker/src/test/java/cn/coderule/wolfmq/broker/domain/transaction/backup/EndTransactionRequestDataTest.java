package cn.coderule.wolfmq.broker.domain.transaction.backup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EndTransactionRequestDataTest {

    @Test
    void testConstructorAndGetters() {
        EndTransactionRequestData data = new EndTransactionRequestData("broker1", null);
        assertEquals("broker1", data.getBrokerName());
        assertNull(data.getRequestHeader());
    }

    @Test
    void testSetters() {
        EndTransactionRequestData data = new EndTransactionRequestData("broker1", null);
        data.setBrokerName("broker2");
        assertEquals("broker2", data.getBrokerName());
    }
}