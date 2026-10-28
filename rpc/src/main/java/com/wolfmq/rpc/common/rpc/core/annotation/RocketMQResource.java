package com.wolfmq.rpc.common.rpc.core.annotation;

import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RocketMQResource {

    ResourceType value();

    String splitter() default "";
}
