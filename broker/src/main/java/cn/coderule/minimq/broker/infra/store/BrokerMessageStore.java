package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedMessageService;
import cn.coderule.minimq.broker.infra.remote.RemoteMessageService;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrokerMessageStore implements MessageStore {
    private final BrokerConfig brokerConfig;
    private final EmbedMessageService embedMessageService;
    private final RemoteMessageService remoteMessageService;

    public BrokerMessageStore(BrokerConfig brokerConfig, EmbedMessageService embedMessageService, RemoteMessageService remoteMessageService) {
        this.brokerConfig = brokerConfig;
        this.embedMessageService = embedMessageService;
        this.remoteMessageService = remoteMessageService;
    }

    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        if (brokerConfig.isEnableEmbedStore()) {
            EnqueueResult result = embedMessageService.enqueue(messageBO);
            if (null != result) {
                return result;
            }
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return EnqueueResult.notAvailable();
        }

        return remoteMessageService.enqueue(messageBO);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        if (brokerConfig.isEnableEmbedStore()) {
            CompletableFuture<EnqueueResult> result = embedMessageService.enqueueAsync(messageBO);
            if (null != result) {
                return result;
            }
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return CompletableFuture.completedFuture(EnqueueResult.notAvailable());
        }

        return remoteMessageService.enqueueAsync(messageBO);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset) {
        if (brokerConfig.isEnableEmbedStore()) {
            GetResult result = embedMessageService.get(topic, queueId, offset);
            if (null != result) {
                return result;
            }
        }

        return remoteMessageService.get(topic, queueId, offset);
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {
        if (brokerConfig.isEnableEmbedStore()) {
            GetResult result = embedMessageService.get(topic, queueId, offset, num);
            if (null != result) {
                return result;
            }
        }

        return remoteMessageService.get(topic, queueId, offset, num);
    }

    @Override
    public GetResult get(GetRequest request) {
        if (brokerConfig.isEnableEmbedStore()) {
            GetResult result = embedMessageService.get(request);
            if (null != result) {
                return result;
            }
        }

        return remoteMessageService.get(request);
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        if (brokerConfig.isEnableEmbedStore()) {
            MessageBO result = embedMessageService.getMessage(topic, queueId, offset);
            if (null != result) {
                return result;
            }
        }

        return remoteMessageService.getMessage(topic, queueId, offset);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        if (brokerConfig.isEnableEmbedStore()) {
            List<MessageBO> result = embedMessageService.getMessage(topic, queueId, offset, num);
            if (null != result) {
                return result;
            }
        }

        return remoteMessageService.getMessage(topic, queueId, offset, num);
    }
}
