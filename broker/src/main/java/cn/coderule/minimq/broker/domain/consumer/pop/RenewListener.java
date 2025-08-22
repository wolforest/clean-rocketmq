package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.core.EventListener;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.revive.RenewEvent;

public class RenewListener implements EventListener<RenewEvent> {
    @Override
    public void fire(RenewEvent event) {
        RequestContext context = createContext(event);
    }

    private RequestContext createContext(RenewEvent event) {
        RequestContext context = RequestContext.create(
            event.getEventType().name()
        );
        context.setChannel(event.getKey().getChannel());

        return context;
    }
}
