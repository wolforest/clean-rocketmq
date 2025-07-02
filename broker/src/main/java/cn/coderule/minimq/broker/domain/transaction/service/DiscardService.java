package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscardService {
    private final MessageService messageService;

    public DiscardService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void discard(MessageBO messageBO) {

    }
}
