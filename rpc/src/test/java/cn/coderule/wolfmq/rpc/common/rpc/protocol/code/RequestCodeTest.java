package cn.coderule.wolfmq.rpc.common.rpc.protocol.code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RequestCodeTest {

    @Test
    void testMessageRelatedCodes() {
        assertEquals(10, RequestCode.SEND_MESSAGE);
        assertEquals(11, RequestCode.PULL_MESSAGE);
        assertEquals(12, RequestCode.QUERY_MESSAGE);
        assertEquals(320, RequestCode.SEND_BATCH_MESSAGE);
        assertEquals(310, RequestCode.SEND_MESSAGE_V2);
    }

    @Test
    void testOffsetRelatedCodes() {
        assertEquals(13, RequestCode.QUERY_BROKER_OFFSET);
        assertEquals(14, RequestCode.QUERY_CONSUMER_OFFSET);
        assertEquals(15, RequestCode.UPDATE_CONSUMER_OFFSET);
    }

    @Test
    void testTopicRelatedCodes() {
        assertEquals(17, RequestCode.UPDATE_AND_CREATE_TOPIC);
        assertEquals(21, RequestCode.GET_ALL_TOPIC_CONFIG);
        assertEquals(22, RequestCode.GET_TOPIC_CONFIG_LIST);
        assertEquals(23, RequestCode.GET_TOPIC_NAME_LIST);
        assertEquals(105, RequestCode.GET_ROUTEINFO_BY_TOPIC);
    }

    @Test
    void testConsumerRelatedCodes() {
        assertEquals(34, RequestCode.HEART_BEAT);
        assertEquals(35, RequestCode.UNREGISTER_CLIENT);
        assertEquals(38, RequestCode.GET_CONSUMER_LIST_BY_GROUP);
        assertEquals(200, RequestCode.UPDATE_AND_CREATE_SUBSCRIPTIONGROUP);
    }

    @Test
    void testTransactionCodes() {
        assertEquals(37, RequestCode.END_TRANSACTION);
        assertEquals(39, RequestCode.CHECK_TRANSACTION_STATE);
    }

    @Test
    void testPopMessageCodes() {
        assertEquals(200050, RequestCode.POP_MESSAGE);
        assertEquals(200051, RequestCode.ACK_MESSAGE);
        assertEquals(200151, RequestCode.BATCH_ACK_MESSAGE);
        assertEquals(200053, RequestCode.CHANGE_MESSAGE_INVISIBLETIME);
    }

    @Test
    void testRegistryCodes() {
        assertEquals(103, RequestCode.REGISTER_BROKER);
        assertEquals(104, RequestCode.UNREGISTER_BROKER);
        assertEquals(901, RequestCode.GET_BROKER_MEMBER_GROUP);
        assertEquals(904, RequestCode.BROKER_HEARTBEAT);
    }

    @Test
    void testControllerCodes() {
        assertEquals(1001, RequestCode.CONTROLLER_ALTER_SYNC_STATE_SET);
        assertEquals(1002, RequestCode.CONTROLLER_ELECT_MASTER);
        assertEquals(1003, RequestCode.CONTROLLER_REGISTER_BROKER);
    }

    @Test
    void testAuthCodes() {
        assertEquals(3001, RequestCode.AUTH_CREATE_USER);
        assertEquals(3006, RequestCode.AUTH_CREATE_ACL);
    }
}
