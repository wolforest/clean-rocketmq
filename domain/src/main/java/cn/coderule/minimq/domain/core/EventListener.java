package cn.coderule.minimq.domain.core;

public interface EventListener<T> {
    void fire(T event);
}
