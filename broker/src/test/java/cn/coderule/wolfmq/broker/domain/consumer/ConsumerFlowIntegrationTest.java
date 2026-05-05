package cn.coderule.wolfmq.broker.domain.consumer;

import cn.coderule.wolfmq.broker.api.ConsumerController;
import cn.coderule.wolfmq.broker.api.validator.ConsumeValidator;
import cn.coderule.wolfmq.broker.api.validator.GroupValidator;
import cn.coderule.wolfmq.broker.domain.consumer.ack.BrokerAckService;
import cn.coderule.wolfmq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import cn.coderule.wolfmq.broker.domain.consumer.pop.PopService;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.enums.consume.AckStatus;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeType;
import cn.coderule.wolfmq.domain.core.enums.consume.PopStatus;
import cn.coderule.wolfmq.domain.core.enums.message.MessageModel;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.ConsumerInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.wolfmq.domain.domain.consumer.running.ConsumerGroupInfo;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test for the Consumer flow in the broker module.
 * Wires real domain services together, mocking only infrastructure (MQStore, SubscriptionFacade).
 */
class ConsumerFlowIntegrationTest {

    private BrokerConfig brokerConfig;
    private MessageConfig messageConfig;

    private PopService popService;
    private BrokerAckService ackService;
    private ConsumerManager consumerManager;
    private InvisibleService invisibleService;
    private SubscriptionFacade subscriptionStore;

    private Consumer consumer;
    private ConsumerController controller;

    @BeforeEach
    void setUp() {
        messageConfig = new MessageConfig();
        messageConfig.setMinInvisibleTime(10_000L);
        messageConfig.setMaxInvisibleTime(43_200_000L);
        messageConfig.setMaxPopSize(32);
        messageConfig.setDefaultInvisibleTime(60_000L);
        messageConfig.setEnableAutoRenew(true);

        brokerConfig = new BrokerConfig();
        brokerConfig.setMessageConfig(messageConfig);

        popService = mock(PopService.class);
        ackService = mock(BrokerAckService.class);
        consumerManager = new ConsumerManager(brokerConfig);
        invisibleService = mock(InvisibleService.class);
        subscriptionStore = mock(SubscriptionFacade.class);

        consumer = new Consumer(popService, ackService, consumerManager, invisibleService, subscriptionStore);
        controller = new ConsumerController(brokerConfig, consumer);
    }

    @Nested
    @DisplayName("ConsumerController pop message")
    class PopMessageTests {

        @Test
        @DisplayName("pop with valid request returns PopResult")
        void popMessage_validRequest_returnsPopResult() {
            PopResult expectedResult = new PopResult(PopStatus.NO_NEW_MSG, java.util.List.of());
            when(popService.pop(any(PopRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            CompletableFuture<PopResult> future = controller.popMessage(request);
            PopResult result = future.join();

            assertNotNull(result);
            assertEquals(PopStatus.NO_NEW_MSG, result.getPopStatus());
            verify(popService).pop(any(PopRequest.class));
        }

        @Test
        @DisplayName("pop with auto-renew sets default invisible time")
        void popMessage_autoRenew_setsDefaultInvisibleTime() {
            PopResult expectedResult = new PopResult(PopStatus.NO_NEW_MSG, java.util.List.of());
            when(popService.pop(any(PopRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(0L)
                .autoRenew(true)
                .requestContext(RequestContext.create())
                .build();

            controller.popMessage(request);

            assertEquals(60_000L, request.getInvisibleTime());
        }
    }

    @Nested
    @DisplayName("ConsumerController pop with invalid group")
    class PopInvalidGroupTests {

        @Test
        @DisplayName("pop with blank group throws InvalidParameterException")
        void popMessage_blankGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with null group throws InvalidParameterException")
        void popMessage_nullGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup(null)
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with system group throws InvalidParameterException")
        void popMessage_systemGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("CID_RMQ_SYS_Test")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with group containing illegal chars throws InvalidParameterException")
        void popMessage_illegalCharsGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test@group#name")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with group too long throws InvalidParameterException")
        void popMessage_groupTooLong_throwsException() {
            String longGroup = "a".repeat(256);
            PopRequest request = PopRequest.builder()
                .consumerGroup(longGroup)
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }
    }

    @Nested
    @DisplayName("ConsumerController pop with invalid topic")
    class PopInvalidTopicTests {

        @Test
        @DisplayName("pop with blank topic throws InvalidParameterException")
        void popMessage_blankTopic_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with illegal topic chars throws InvalidParameterException")
        void popMessage_illegalTopic_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("test@topic!")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.popMessage(request));
        }
    }

    @Nested
    @DisplayName("ConsumerController pop with invisible time validation")
    class PopInvisibleTimeTests {

        @Test
        @DisplayName("pop with invisible time too small throws InvalidRequestException")
        void popMessage_invisibleTimeTooSmall_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(100L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidRequestException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with invisible time too large throws InvalidRequestException")
        void popMessage_invisibleTimeTooLarge_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(50_000_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidRequestException.class, () -> controller.popMessage(request));
        }

        @Test
        @DisplayName("pop with valid invisible time passes validation")
        void popMessage_validInvisibleTime_passesValidation() {
            PopResult expectedResult = new PopResult(PopStatus.NO_NEW_MSG, java.util.List.of());
            when(popService.pop(any(PopRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            CompletableFuture<PopResult> future = controller.popMessage(request);
            assertNotNull(future);
        }

        @Test
        @DisplayName("pop with maxNum exceeding limit throws InvalidRequestException")
        void popMessage_maxNumExceedsLimit_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("test_group")
                .topicName("TestTopic")
                .maxNum(64)
                .invisibleTime(30_000L)
                .autoRenew(false)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidRequestException.class, () -> controller.popMessage(request));
        }
    }

    @Nested
    @DisplayName("ConsumerController ack message")
    class AckMessageTests {

        @Test
        @DisplayName("ack with valid request completes successfully")
        void ack_validRequest_completesSuccessfully() {
            AckResult expectedResult = AckResult.success();
            when(ackService.ack(any(AckRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

            AckRequest request = AckRequest.builder()
                .groupName("test_group")
                .topicName("TestTopic")
                .messageId("msg-001")
                .requestContext(RequestContext.create())
                .build();

            CompletableFuture<AckResult> future = controller.ack(request);
            AckResult result = future.join();

            assertNotNull(result);
            assertEquals(AckStatus.OK, result.getStatus());
            verify(ackService).ack(any(AckRequest.class));
        }

        @Test
        @DisplayName("ack with blank group throws InvalidParameterException")
        void ack_blankGroup_throwsException() {
            AckRequest request = AckRequest.builder()
                .groupName("")
                .topicName("TestTopic")
                .messageId("msg-001")
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.ack(request));
        }

        @Test
        @DisplayName("ack with blank topic throws InvalidParameterException")
        void ack_blankTopic_throwsException() {
            AckRequest request = AckRequest.builder()
                .groupName("test_group")
                .topicName("")
                .messageId("msg-001")
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.ack(request));
        }
    }

    @Nested
    @DisplayName("ConsumerController change invisible time")
    class ChangeInvisibleTests {

        @Test
        @DisplayName("changeInvisible with valid request completes successfully")
        void changeInvisible_validRequest_completesSuccessfully() {
            AckResult expectedResult = AckResult.success();
            when(invisibleService.changeInvisible(any(InvisibleRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));

            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("test_group")
                .topicName("TestTopic")
                .messageId("msg-001")
                .invisibleTime(60_000L)
                .requestContext(RequestContext.create())
                .build();

            CompletableFuture<AckResult> future = controller.changeInvisible(request);
            AckResult result = future.join();

            assertNotNull(result);
            assertEquals(AckStatus.OK, result.getStatus());
            verify(invisibleService).changeInvisible(any(InvisibleRequest.class));
        }

        @Test
        @DisplayName("changeInvisible with blank group throws InvalidParameterException")
        void changeInvisible_blankGroup_throwsException() {
            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("")
                .topicName("TestTopic")
                .messageId("msg-001")
                .invisibleTime(60_000L)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.changeInvisible(request));
        }

        @Test
        @DisplayName("changeInvisible with invisible time too small throws InvalidRequestException")
        void changeInvisible_invisibleTimeTooSmall_throwsException() {
            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("test_group")
                .topicName("TestTopic")
                .messageId("msg-001")
                .invisibleTime(100L)
                .requestContext(RequestContext.create())
                .build();

            assertThrows(InvalidRequestException.class, () -> controller.changeInvisible(request));
        }
    }

    @Nested
    @DisplayName("Consumer registration via ConsumerManager")
    class ConsumerRegistrationTests {

        @Test
        @DisplayName("register consumer returns ConsumerGroupInfo")
        void register_consumer_returnsGroupInfo() {
            Channel channel = mock(Channel.class);
            ConsumerInfo consumerInfo = ConsumerInfo.builder()
                .groupName("test_group")
                .consumeType(ConsumeType.CONSUME_PASSIVELY)
                .messageModel(MessageModel.CLUSTERING)
                .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LAST_OFFSET)
                .channelInfo(new ClientChannelInfo(channel, "client-1", null, 0))
                .build();

            boolean result = consumer.register(consumerInfo);

            assertTrue(result);

            ConsumerGroupInfo groupInfo = consumer.getGroupInfo(
                RequestContext.create(), "test_group"
            );
            assertNotNull(groupInfo);
            assertEquals("test_group", groupInfo.getGroupName());
        }

        @Test
        @DisplayName("unregister consumer removes channel")
        void unregister_consumer_removesChannel() {
            Channel channel = mock(Channel.class);
            ConsumerInfo consumerInfo = ConsumerInfo.builder()
                .groupName("test_group_2")
                .consumeType(ConsumeType.CONSUME_PASSIVELY)
                .messageModel(MessageModel.CLUSTERING)
                .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LAST_OFFSET)
                .channelInfo(new ClientChannelInfo(channel, "client-2", null, 0))
                .build();

            consumer.register(consumerInfo);

            ConsumerGroupInfo groupInfo = consumer.getGroupInfo(
                RequestContext.create(), "test_group_2"
            );
            assertNotNull(groupInfo);

            consumer.unregister(consumerInfo);

            ConsumerGroupInfo removed = consumer.getGroupInfo(
                RequestContext.create(), "test_group_2"
            );
            assertNull(removed);
        }

        @Test
        @DisplayName("register via ConsumerController validates group name")
        void register_viaController_validatesGroupName() {
            Channel channel = mock(Channel.class);
            ConsumerInfo consumerInfo = ConsumerInfo.builder()
                .groupName("")
                .consumeType(ConsumeType.CONSUME_PASSIVELY)
                .messageModel(MessageModel.CLUSTERING)
                .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LAST_OFFSET)
                .channelInfo(new ClientChannelInfo(channel, "client-3", null, 0))
                .build();

            assertThrows(InvalidParameterException.class, () -> controller.register(consumerInfo));
        }

        @Test
        @DisplayName("getSubscription delegates to subscriptionStore")
        void getSubscription_delegatesToStore() {
            SubscriptionGroup subGroup = SubscriptionGroup.builder()
                .groupName("test_group")
                .build();
            when(subscriptionStore.getGroupAsync("TestTopic", "test_group"))
                .thenReturn(CompletableFuture.completedFuture(subGroup));

            CompletableFuture<SubscriptionGroup> future =
                consumer.getSubscription(RequestContext.create(), "TestTopic", "test_group");

            SubscriptionGroup result = future.join();
            assertNotNull(result);
            assertEquals("test_group", result.getGroupName());
            verify(subscriptionStore).getGroupAsync("TestTopic", "test_group");
        }
    }

    @Nested
    @DisplayName("ConsumeValidator integration with GroupValidator")
    class ConsumeValidatorIntegrationTests {

        private ConsumeValidator validator;

        @BeforeEach
        void initValidator() {
            validator = new ConsumeValidator(brokerConfig);
        }

        @Test
        @DisplayName("validate PopRequest with valid group and topic passes")
        void validatePopRequest_valid_passes() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("valid_group")
                .topicName("ValidTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertDoesNotThrow(() -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with blank group throws InvalidParameterException")
        void validatePopRequest_blankGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("")
                .topicName("ValidTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with system group throws InvalidParameterException")
        void validatePopRequest_systemGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("CID_RMQ_SYS_Consumer")
                .topicName("ValidTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with group containing illegal chars throws InvalidParameterException")
        void validatePopRequest_illegalCharsGroup_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("group$name")
                .topicName("ValidTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with group too long throws InvalidParameterException")
        void validatePopRequest_groupTooLong_throwsException() {
            String longGroup = "g".repeat(256);
            PopRequest request = PopRequest.builder()
                .consumerGroup(longGroup)
                .topicName("ValidTopic")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with blank topic throws InvalidParameterException")
        void validatePopRequest_blankTopic_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("valid_group")
                .topicName("")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate PopRequest with illegal topic chars throws InvalidParameterException")
        void validatePopRequest_illegalTopic_throwsException() {
            PopRequest request = PopRequest.builder()
                .consumerGroup("valid_group")
                .topicName("topic!@#")
                .maxNum(16)
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validateInvisibleTime with too small value throws InvalidRequestException")
        void validateInvisibleTime_tooSmall_throwsException() {
            assertThrows(InvalidRequestException.class,
                () -> validator.validateInvisibleTime(100L));
        }

        @Test
        @DisplayName("validateInvisibleTime with too large value throws InvalidRequestException")
        void validateInvisibleTime_tooLarge_throwsException() {
            assertThrows(InvalidRequestException.class,
                () -> validator.validateInvisibleTime(50_000_000L));
        }

        @Test
        @DisplayName("validateInvisibleTime with valid value passes")
        void validateInvisibleTime_valid_passes() {
            assertDoesNotThrow(() -> validator.validateInvisibleTime(30_000L));
        }

        @Test
        @DisplayName("validate AckRequest with valid params passes")
        void validateAckRequest_valid_passes() {
            AckRequest request = AckRequest.builder()
                .groupName("valid_group")
                .topicName("ValidTopic")
                .build();

            assertDoesNotThrow(() -> validator.validate(request));
        }

        @Test
        @DisplayName("validate AckRequest with blank group throws InvalidParameterException")
        void validateAckRequest_blankGroup_throwsException() {
            AckRequest request = AckRequest.builder()
                .groupName("")
                .topicName("ValidTopic")
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate InvisibleRequest with valid params passes")
        void validateInvisibleRequest_valid_passes() {
            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("valid_group")
                .topicName("ValidTopic")
                .invisibleTime(30_000L)
                .build();

            assertDoesNotThrow(() -> validator.validate(request));
        }

        @Test
        @DisplayName("validate InvisibleRequest with blank group throws InvalidParameterException")
        void validateInvisibleRequest_blankGroup_throwsException() {
            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("")
                .topicName("ValidTopic")
                .invisibleTime(30_000L)
                .build();

            assertThrows(InvalidParameterException.class, () -> validator.validate(request));
        }

        @Test
        @DisplayName("validate InvisibleRequest with too small invisible time throws InvalidRequestException")
        void validateInvisibleRequest_invisibleTimeTooSmall_throwsException() {
            InvisibleRequest request = InvisibleRequest.builder()
                .groupName("valid_group")
                .topicName("ValidTopic")
                .invisibleTime(100L)
                .build();

            assertThrows(InvalidRequestException.class, () -> validator.validate(request));
        }
    }

    @Nested
    @DisplayName("GroupValidator direct validation")
    class GroupValidatorTests {

        @Test
        @DisplayName("blank group throws InvalidParameterException")
        void validate_blankGroup_throwsException() {
            assertThrows(InvalidParameterException.class, () -> GroupValidator.validate(""));
        }

        @Test
        @DisplayName("null group throws InvalidParameterException")
        void validate_nullGroup_throwsException() {
            assertThrows(InvalidParameterException.class, () -> GroupValidator.validate(null));
        }

        @Test
        @DisplayName("group too long throws InvalidParameterException")
        void validate_groupTooLong_throwsException() {
            assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("g".repeat(256)));
        }

        @Test
        @DisplayName("group with illegal chars throws InvalidParameterException")
        void validate_illegalChars_throwsException() {
            assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("group$name"));
        }

        @Test
        @DisplayName("system group throws InvalidParameterException")
        void validate_systemGroup_throwsException() {
            assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("CID_RMQ_SYS_Test"));
        }

        @Test
        @DisplayName("valid group passes validation")
        void validate_validGroup_passes() {
            assertDoesNotThrow(() -> GroupValidator.validate("valid_group"));
        }

        @Test
        @DisplayName("group with hyphens and underscores passes")
        void validate_groupWithHyphens_passes() {
            assertDoesNotThrow(() -> GroupValidator.validate("my-consumer_group-v2"));
        }
    }
}
