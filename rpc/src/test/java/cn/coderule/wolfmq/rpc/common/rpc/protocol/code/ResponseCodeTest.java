package cn.coderule.wolfmq.rpc.common.rpc.protocol.code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseCodeTest {

    @Test
    void testSuccessCodes() {
        assertEquals(0, ResponseCode.SUCCESS);
    }

    @Test
    void testSystemErrorCodes() {
        assertEquals(1, ResponseCode.SYSTEM_ERROR);
        assertEquals(2, ResponseCode.SYSTEM_BUSY);
    }

    @Test
    void testTopicRelatedCodes() {
        assertEquals(17, ResponseCode.TOPIC_NOT_EXIST);
        assertEquals(18, ResponseCode.TOPIC_EXIST_ALREADY);
    }

    @Test
    void testSubscriptionCodes() {
        assertEquals(24, ResponseCode.SUBSCRIPTION_NOT_EXIST);
        assertEquals(26, ResponseCode.SUBSCRIPTION_GROUP_NOT_EXIST);
    }

    @Test
    void testTransactionCodes() {
        assertEquals(200, ResponseCode.TRANSACTION_SHOULD_COMMIT);
        assertEquals(201, ResponseCode.TRANSACTION_SHOULD_ROLLBACK);
        assertEquals(202, ResponseCode.TRANSACTION_STATE_UNKNOW);
    }

    @Test
    void testPullMessageCodes() {
        assertEquals(19, ResponseCode.PULL_NOT_FOUND);
        assertEquals(20, ResponseCode.PULL_RETRY_IMMEDIATELY);
        assertEquals(21, ResponseCode.PULL_OFFSET_MOVED);
    }

    @Test
    void testControllerCodes() {
        assertEquals(2000, ResponseCode.CONTROLLER_FENCED_MASTER_EPOCH);
        assertEquals(2007, ResponseCode.CONTROLLER_NOT_LEADER);
        assertEquals(2010, ResponseCode.CONTROLLER_BROKER_NEED_TO_BE_REGISTERED);
    }

    @Test
    void testRpcCodes() {
        assertEquals(-1000, ResponseCode.RPC_UNKNOWN);
        assertEquals(-1002, ResponseCode.RPC_ADDR_IS_NULL);
        assertEquals(-1006, ResponseCode.RPC_TIME_OUT);
    }

    @Test
    void testAuthCodes() {
        assertEquals(3001, ResponseCode.USER_NOT_EXIST);
        assertEquals(3002, ResponseCode.POLICY_NOT_EXIST);
    }

    @Test
    void testInheritanceFromSystemResponseCode() {
        assertTrue(ResponseCode.SUCCESS == SystemResponseCode.SUCCESS);
        assertTrue(ResponseCode.SYSTEM_ERROR == SystemResponseCode.SYSTEM_ERROR);
        assertTrue(ResponseCode.SYSTEM_BUSY == SystemResponseCode.SYSTEM_BUSY);
    }
}
