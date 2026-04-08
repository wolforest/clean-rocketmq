package cn.coderule.wolfmq.broker.domain.transaction.check.context;

import cn.coderule.wolfmq.broker.domain.transaction.check.service.CheckService;
import cn.coderule.wolfmq.broker.domain.transaction.check.service.DiscardService;
import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.transaction.CommitBuffer;
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
