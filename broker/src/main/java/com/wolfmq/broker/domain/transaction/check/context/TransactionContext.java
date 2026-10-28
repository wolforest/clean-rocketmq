package com.wolfmq.broker.domain.transaction.check.context;

import com.wolfmq.broker.domain.transaction.check.service.CheckService;
import com.wolfmq.broker.domain.transaction.check.service.DiscardService;
import com.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.transaction.CommitBuffer;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContext implements Serializable {
    private BrokerConfig brokerConfig;
    private CommitBuffer commitBuffer;

    private CheckService checkService;
    private DiscardService discardService;
    private TransactionMessageService messageService;

}
