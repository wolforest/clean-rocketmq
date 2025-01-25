package com.wolf.minimq.broker.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.broker.domain.consumer.ConsumerManager;
import com.wolf.minimq.broker.domain.producer.ProducerManager;
import com.wolf.minimq.broker.domain.transaction.TransactionManager;
import com.wolf.minimq.broker.infra.StoreManager;
import com.wolf.minimq.broker.server.grpc.GrpcManager;
import com.wolf.minimq.domain.service.store.manager.MetaManager;
import com.wolf.minimq.store.domain.meta.DefaultMetaManager;
import com.wolf.minimq.store.server.StoreContext;

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
