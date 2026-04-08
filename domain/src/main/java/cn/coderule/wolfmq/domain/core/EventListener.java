package cn.coderule.wolfmq.domain.core;

public interface EventListener<T> {
    void fire(T event);
}
