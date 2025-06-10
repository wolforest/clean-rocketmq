package cn.coderule.minimq.domain.service.common;

import cn.coderule.common.convention.container.EventBus;
import java.util.function.Consumer;

public class ServerEventManager {
    private final EventBus instance = new EventBus();

    public void on(ServerEvent event, Consumer<Object> listener) {
        instance.on(event, listener);
    }

    public void off(ServerEvent event, Consumer<Object> listener) {
        instance.off(event, listener);
    }

    public void emit(ServerEvent event, Object arg) {
        instance.emit(event, arg);
    }
}
