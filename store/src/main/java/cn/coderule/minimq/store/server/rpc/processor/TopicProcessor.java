package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.enums.MessageType;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.utils.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.store.protocol.header.CreateTopicRequestHeader;
import cn.coderule.minimq.rpc.store.protocol.header.DeleteTopicRequestHeader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicProcessor implements RpcProcessor {
    private final TopicConfig topicConfig;
    private final TopicStore topicStore;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_ALL_TOPIC_CONFIG,
        RequestCode.DELETE_TOPIC_IN_BROKER,
        RequestCode.UPDATE_AND_CREATE_TOPIC
    );

    public TopicProcessor(TopicConfig topicConfig, TopicStore topicStore, ExecutorService executor) {
        this.topicConfig = topicConfig;
        this.topicStore = topicStore;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_AND_CREATE_TOPIC -> this.saveTopic(ctx, request);
            case RequestCode.GET_ALL_TOPIC_CONFIG -> this.getTopicList(ctx, request);
            case RequestCode.DELETE_TOPIC_IN_BROKER -> this.deleteTopic(ctx, request);
            case RequestCode.GET_TOPIC_STATS_INFO -> this.getTopicStats(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand saveTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        CreateTopicRequestHeader requestHeader = request.decodeHeader(CreateTopicRequestHeader.class);
        log.info("receive request to update or create topic={}, caller address={}",
            requestHeader.getTopic(), NettyHelper.getRemoteAddr(ctx.channel()));

        if (isTopicNameInvalid(requestHeader.getTopic(), response)) {
            return response;
        }

        Topic topic = requestHeader.toTopic();
        if (!validateTopicType(topic.getTopicType(), response)) {
            return response;
        }

        if (topicStore.exists(requestHeader.getTopic())) {
            return response.success();
        }

        try {
            topicStore.saveTopic(topic);
        } catch (Exception e) {
            log.error("save topic={} error", topic.getTopicName(), e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "save topic error");
        }

        return response.success();
    }

    private RpcCommand getTopicList(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand getTopicStats(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        return response.success();
    }

    private RpcCommand deleteTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        DeleteTopicRequestHeader requestHeader = request.decodeHeader(DeleteTopicRequestHeader.class);
        log.info("receive request to delete topic={}, caller address={}",
            requestHeader.getTopic(), NettyHelper.getRemoteAddr(ctx.channel()));

        if (isTopicNameInvalid(requestHeader.getTopic(), response)) {
            return response;
        }

        try {
            topicStore.deleteTopic(requestHeader.getTopic());
        } catch (Exception e) {
            log.error("delete topic={} error", requestHeader.getTopic(), e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "delete topic error");
        }

        return response.success();
    }

    private boolean isTopicNameInvalid(String topicName, RpcCommand response) {
        if (StringUtil.isBlank(topicName)) {
            response.setCodeAndRemark(
                ResponseCode.SYSTEM_ERROR,
                "TopicName can't be blank"
            );
            return true;
        }

        TopicValidator.ValidateTopicResult validateResult = TopicValidator.validateTopic(topicName);
        if (!validateResult.isValid()) {
            response.setCodeAndRemark(
                ResponseCode.SYSTEM_ERROR,
                validateResult.getRemark()
            );
            return true;
        }

        if (TopicValidator.isSystemTopic(topicName)) {
            response.setCodeAndRemark(
                ResponseCode.SYSTEM_ERROR,
                "can't use system topic: " + topicName
            );
            return true;
        }

        return false;
    }

    private boolean validateTopicType(MessageType type, RpcCommand response) {
        if (MessageType.MIXED == type && !topicConfig.isEnableMixedMessageType()) {
            response.setCodeAndRemark(
                ResponseCode.SYSTEM_ERROR,
                "mixed message type is not enabled"
            );
            return false;
        }

        return true;
    }

}
