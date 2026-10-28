package com.wolfmq.domain.domain.consumer.receipt;

public interface EventListener<T> {
    void fire(T event);
}
