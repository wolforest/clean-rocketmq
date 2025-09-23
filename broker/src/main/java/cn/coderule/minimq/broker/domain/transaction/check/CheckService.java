package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckService implements Lifecycle {
    private final TransactionConfig transactionConfig;
    private final ExecutorService executor;

    public CheckService(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;
        this.executor = initExecutor();
    }



    public void check(MessageBO messageBO) {

    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
    }

    private void checkAsync(MessageBO messageBO) {

    }

    private ExecutorService initExecutor() {
        return ThreadUtil.newThreadPoolExecutor(
            transactionConfig.getCheckThreadNum(),
            transactionConfig.getMaxCheckThreadNum(),
            transactionConfig.getKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(transactionConfig.getCheckQueueCapacity()),
            new DefaultThreadFactory("TransactionCheckThread_"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
