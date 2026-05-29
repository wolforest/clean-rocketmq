package cn.coderule.wolfmq.rpc.broker.rpc.protocol.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndTransactionResponseHeaderTest {

    @Test
    void testConstructor() {
        EndTransactionResponseHeader header = new EndTransactionResponseHeader();
        assertNotNull(header);
    }

    @Test
    void testCheckFields() throws Exception {
        EndTransactionResponseHeader header = new EndTransactionResponseHeader();
        header.checkFields();
    }
}