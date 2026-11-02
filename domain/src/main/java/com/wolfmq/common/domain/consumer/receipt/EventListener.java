package com.wolfmq.common.domain.consumer.receipt;

public interface EventListener<T> {
    void fire(T event);
}
