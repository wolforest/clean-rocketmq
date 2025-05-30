package cn.coderule.minimq.rpc.common.rpc.core.annotation;

import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RocketMQAction {

    int value();

    ResourceType resource() default ResourceType.UNKNOWN;

    Action[] action();
}
