package cn.coderule.wolfmq.broker.domain.transaction.check.service;

import cn.coderule.wolfmq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.wolfmq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.message.MessageStatus;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrepareMessageCheckerTest {

    private TransactionContext transactionContext;
    private BrokerConfig brokerConfig;
    private TransactionConfig transactionConfig;
    private MessageConfig messageConfig;
    private OperationMessageLoader operationMessageLoader;
    private CheckService checkService;
    private DiscardService discardService;
    private TransactionMessageService messageService;

    private PrepareMessageChecker checker;

    @BeforeEach
    void setUp() {
        transactionContext = mock(TransactionContext.class);
        brokerConfig = mock(BrokerConfig.class);
        transactionConfig = mock(TransactionConfig.class);
        messageConfig = mock(MessageConfig.class);
        operationMessageLoader = mock(OperationMessageLoader.class);
        checkService = mock(CheckService.class);
        discardService = mock(DiscardService.class);
        messageService = mock(TransactionMessageService.class);

        when(transactionContext.getBrokerConfig()).thenReturn(brokerConfig);
        when(brokerConfig.getTransactionConfig()).thenReturn(transactionConfig);
        when(brokerConfig.getMessageConfig()).thenReturn(messageConfig);
        when(transactionContext.getCheckService()).thenReturn(checkService);
        when(transactionContext.getDiscardService()).thenReturn(discardService);
        when(transactionContext.getMessageService()).thenReturn(messageService);

        when(transactionConfig.getMaxCheckTimes()).thenReturn(15);
        when(transactionConfig.getTransactionTimeout()).thenReturn(6000L);
        when(messageConfig.getFileReservedTime()).thenReturn(72 * 3600 * 1000);

        checker = new PrepareMessageChecker(transactionContext, operationMessageLoader);
    }

    @Test
    void check_emptyResult_overflowOne_returnsFalse() {
        CheckContext checkContext = buildCheckContext();
        DequeueResult emptyResult = DequeueResult.builder()
            .status(MessageStatus.OFFSET_OVERFLOW_ONE)
            .messageList(Collections.emptyList())
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(emptyResult);

        boolean result = checker.check(checkContext);

        assertFalse(result);
        verify(discardService, never()).discard(any());
    }

    @Test
    void check_emptyResult_invalidCountExceedsMax_returnsFalse() {
        CheckContext checkContext = buildCheckContext();
        DequeueResult emptyResult = DequeueResult.builder()
            .status(MessageStatus.NO_MATCHED_MESSAGE)
            .messageList(Collections.emptyList())
            .nextOffset(100L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(emptyResult);

        boolean result = checker.check(checkContext);

        assertFalse(result);
    }

    @Test
    void check_emptyResult_offsetIllegal_updatesCounterAndReturnsTrue() {
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(0)
            .startTime(System.currentTimeMillis() - 10000)
            .build();

        DequeueResult emptyResult = DequeueResult.builder()
            .status(MessageStatus.OFFSET_FOUND_NULL)
            .messageList(Collections.emptyList())
            .nextOffset(50L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(emptyResult);

        boolean result = checker.check(checkContext);

        assertTrue(result);
        assertEquals(50L, checkContext.getPrepareCounter());
    }

    @Test
    void check_overMaxCheckTimes_shouldDiscard() {
        CheckContext checkContext = buildCheckContext();
        MessageBO message = buildMessage(100L, System.currentTimeMillis() - 5000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "15");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(discardService).discard(message);
    }

    @Test
    void check_overMaxCheckTimes_incrementsPrepareCounter() {
        CheckContext checkContext = buildCheckContext();
        MessageBO message = buildMessage(100L, System.currentTimeMillis() - 5000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "15");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        long initialCounter = checkContext.getPrepareCounter();
        checker.check(checkContext);

        assertEquals(initialCounter + 1, checkContext.getPrepareCounter());
    }

    @Test
    void check_messageExpired_shouldDiscard() {
        CheckContext checkContext = buildCheckContext();
        long bornTime = System.currentTimeMillis() - (100L * 3600 * 1000L);
        MessageBO message = buildMessage(100L, bornTime);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(discardService).discard(message);
    }

    @Test
    void check_messageNotExpired_shouldNotDiscard() {
        CheckContext checkContext = buildCheckContext();
        long bornTime = System.currentTimeMillis() - 1000;
        MessageBO message = buildMessage(100L, bornTime);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        checker.check(checkContext);
        verify(discardService, never()).discard(any());
    }

    @Test
    void check_freshMessage_storeAfterStartTime_returnsFalse() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 1000)
            .build();

        MessageBO message = buildMessage(100L, now - 500);
        message.setStoreTimestamp(now);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        boolean checkResult = checker.check(checkContext);

        assertFalse(checkResult);
    }

    @Test
    void check_positiveCheckTime_proceedsToImmunityCheck() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 10000)
            .build();

        long bornTime = now - 100;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 20000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, "60");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
    }

    @Test
    void check_inImmunityPeriod_needCheck_callsCheckService() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(checkService).check(message);
        assertEquals(1, checkContext.getMessageCheckCount());
    }

    @Test
    void check_checkImmunityTimeReturnsNull_whenCheckPrepareQueueOffsetReturnsTrue() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 100;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, "3600");
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, "100");
        checkContext.getOffsetMap().put(100L, 200L);

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        assertEquals(1, checkContext.getPrepareCounter());
    }

    @Test
    void check_revivePrepareMessageFails_returnsTrueWithoutCheck() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(EnqueueResult.failure());

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(checkService, never()).check(any());
    }

    @Test
    void check_messageAgeNegative_proceedsToImmunityCheck() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now + 100000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
    }

    @Test
    void check_explicitCheckImmunityTime_messageAgeExceedsCheckTime_callsCheckService() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, "5");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        checker.check(checkContext);

        verify(checkService).check(message);
    }

    @Test
    void check_positiveCheckImmunityTime_notTimedOut_proceedsToImmunityCheck() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, "60");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
    }

    @Test
    void check_checkTimesBelowMax_incrementsCheckTimes() {
        CheckContext checkContext = buildCheckContext();
        MessageBO message = buildMessage(100L, System.currentTimeMillis() - 5000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "5");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        checker.check(checkContext);

        assertEquals("6", message.getProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES));
    }

    @Test
    void check_prepareQueueOffsetNull_reviveSucceeds_callsCheckService() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-revived")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        checker.check(checkContext);

        verify(checkService).check(message);
        assertEquals(200L, message.getQueueOffset());
        assertEquals(200L, message.getCommitOffset());
    }

    @Test
    void check_prepareQueueOffsetNegative_checkPrepareQueueOffsetReturnsFalse() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, "-1");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(checkService).check(message);
    }

    @Test
    void check_prepareQueueOffsetNotInContext_reviveSucceeds_callsCheckService() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, "999");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);

        EnqueueResult enqueueResult = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .queueOffset(200L)
            .commitOffset(200L)
            .messageId("msg-1")
            .build();
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(enqueueResult);

        checker.check(checkContext);

        verify(checkService).check(message);
    }

    @Test
    void check_prepareQueueOffsetNotInContext_reviveFails_returnsTrue() {
        long now = System.currentTimeMillis();
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(now - 60000)
            .build();

        long bornTime = now - 30000;
        MessageBO message = buildMessage(100L, bornTime);
        message.setStoreTimestamp(now - 70000);
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, "1");
        message.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, "999");

        DequeueResult result = DequeueResult.builder()
            .status(MessageStatus.FOUND)
            .messageList(List.of(message))
            .nextOffset(101L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(result);
        when(messageService.enqueueMessage(any(MessageBO.class))).thenReturn(EnqueueResult.failure());

        boolean checkResult = checker.check(checkContext);

        assertTrue(checkResult);
        verify(checkService, never()).check(any());
    }

    @Test
    void check_emptyResult_noOverflow_illegalOffset_updatesNextOffset() {
        CheckContext checkContext = CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(10L)
            .prepareCounter(10L)
            .invalidPrepareMessageCount(0)
            .startTime(System.currentTimeMillis() - 10000)
            .build();

        DequeueResult emptyResult = DequeueResult.builder()
            .status(MessageStatus.OFFSET_OVERFLOW_BADLY)
            .messageList(Collections.emptyList())
            .nextOffset(55L)
            .build();

        when(messageService.getMessage(any(), anyLong(), anyInt())).thenReturn(emptyResult);

        boolean result = checker.check(checkContext);

        assertTrue(result);
        assertEquals(55L, checkContext.getPrepareCounter());
    }

    private CheckContext buildCheckContext() {
        return CheckContext.builder()
            .prepareQueue(new MessageQueue("topic", "group", 0))
            .operationQueue(new MessageQueue("topic", "group", 1))
            .prepareOffset(0L)
            .prepareCounter(0L)
            .invalidPrepareMessageCount(1)
            .startTime(System.currentTimeMillis() - 10000)
            .build();
    }

    private MessageBO buildMessage(long queueOffset, long bornTimestamp) {
        return MessageBO.builder()
            .topic("test-topic")
            .queueId(0)
            .queueOffset(queueOffset)
            .bornTimestamp(bornTimestamp)
            .storeTimestamp(bornTimestamp + 1000)
            .body("test-body".getBytes())
            .messageId("msg-" + queueOffset)
            .properties(new java.util.HashMap<>())
            .build();
    }
}