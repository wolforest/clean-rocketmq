package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.common.config.business.TransactionConfig;
import com.wolfmq.common.domain.message.property.MessageConst;
import com.wolfmq.common.core.exception.InvalidRequestException;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.transaction.SubmitRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SubmitValidatorTest {

    private TransactionConfig transactionConfig;
    private SubmitValidator validator;

    @BeforeEach
    void setUp() {
        transactionConfig = mock(TransactionConfig.class);
        when(transactionConfig.getTransactionTimeout()).thenReturn(6000L);
        validator = new SubmitValidator(transactionConfig);
    }

    @Test
    void testValidateNullMessage() {
        SubmitRequest request = mock(SubmitRequest.class);
        assertThrows(InvalidRequestException.class, () -> validator.validate(request, null));
    }

    @Test
    void testValidateMismatchedProducerGroup() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getProducerGroup()).thenReturn("groupA");
        when(request.getCommitOffset()).thenReturn(100L);
        when(request.getQueueOffset()).thenReturn(5L);

        MessageBO message = mock(MessageBO.class);
        when(message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP)).thenReturn("groupB");
        when(message.getCommitOffset()).thenReturn(100L);
        when(message.getQueueOffset()).thenReturn(5L);

        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }

    @Test
    void testValidateMismatchedCommitOffset() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getProducerGroup()).thenReturn("groupA");
        when(request.getCommitOffset()).thenReturn(100L);
        when(request.getQueueOffset()).thenReturn(5L);

        MessageBO message = mock(MessageBO.class);
        when(message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP)).thenReturn("groupA");
        when(message.getCommitOffset()).thenReturn(200L);
        when(message.getQueueOffset()).thenReturn(5L);

        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }

    @Test
    void testValidateMismatchedQueueOffset() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getProducerGroup()).thenReturn("groupA");
        when(request.getCommitOffset()).thenReturn(100L);
        when(request.getQueueOffset()).thenReturn(5L);

        MessageBO message = mock(MessageBO.class);
        when(message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP)).thenReturn("groupA");
        when(message.getCommitOffset()).thenReturn(100L);
        when(message.getQueueOffset()).thenReturn(10L);

        assertThrows(InvalidRequestException.class, () -> validator.validate(request, message));
    }

    @Test
    void testValidateSuccess() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getProducerGroup()).thenReturn("groupA");
        when(request.getCommitOffset()).thenReturn(100L);
        when(request.getQueueOffset()).thenReturn(5L);

        MessageBO message = mock(MessageBO.class);
        when(message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP)).thenReturn("groupA");
        when(message.getCommitOffset()).thenReturn(100L);
        when(message.getQueueOffset()).thenReturn(5L);
        when(message.getBornTimestamp()).thenReturn(System.currentTimeMillis());

        assertDoesNotThrow(() -> validator.validate(request, message));
    }

    @Test
    void testValidateFromCheckBypassesCheckTime() {
        SubmitRequest request = mock(SubmitRequest.class);
        when(request.getProducerGroup()).thenReturn("groupA");
        when(request.getCommitOffset()).thenReturn(100L);
        when(request.getQueueOffset()).thenReturn(5L);
        when(request.isFromCheck()).thenReturn(true);

        MessageBO message = mock(MessageBO.class);
        when(message.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP)).thenReturn("groupA");
        when(message.getCommitOffset()).thenReturn(100L);
        when(message.getQueueOffset()).thenReturn(5L);

        assertDoesNotThrow(() -> validator.validate(request, message));
    }
}
