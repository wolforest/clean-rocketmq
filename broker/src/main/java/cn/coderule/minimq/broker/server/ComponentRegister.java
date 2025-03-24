package cn.coderule.minimq.broker.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.broker.domain.consumer.ConsumerManager;
import cn.coderule.minimq.broker.domain.producer.ProducerManager;
import cn.coderule.minimq.broker.domain.transaction.TransactionManager;
import cn.coderule.minimq.broker.infra.StoreManager;
import cn.coderule.minimq.broker.server.grpc.GrpcManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {
        registerGrpc();

        registerStore();

        registerTransaction();
        registerProducer();
        registerConsumer();

        return this.manager;
    }

    private void registerGrpc() {
        GrpcManager component = new GrpcManager();
        manager.register(component);
    }

    private void registerStore() {
        StoreManager component = new StoreManager();
        manager.register(component);
    }

    private void registerProducer() {
        ProducerManager component = new ProducerManager();
        manager.register(component);
    }

    private void registerConsumer() {
        ConsumerManager component = new ConsumerManager();
        manager.register(component);
    }

    private void registerTransaction() {
        TransactionManager component = new TransactionManager();
        manager.register(component);
    }

}
