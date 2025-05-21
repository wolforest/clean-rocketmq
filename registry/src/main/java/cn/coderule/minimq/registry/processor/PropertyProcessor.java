package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.lang.bean.BeanUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.registry.domain.property.PropertyService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyProcessor implements RpcProcessor {
    private final PropertyService propertyService;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.UPDATE_NAMESRV_CONFIG,
        RequestCode.GET_NAMESRV_CONFIG
    );

    public PropertyProcessor(PropertyService propertyService, ExecutorService executor) {
        this.propertyService = propertyService;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_NAMESRV_CONFIG -> this.setProperty(ctx, request);
            case RequestCode.GET_NAMESRV_CONFIG -> this.getProperty(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getProperty(RpcContext ctx, RpcCommand request) {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        String content = propertyService.getString();
        if (StringUtil.isBlank(content)) {
            return response.setCodeAndRemark(SystemResponseCode.SUCCESS, "no property");
        }

        try {
            response.setBody(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("getProperty error", e);
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "getProperty error " + e);
        }

        return response.success();
    }

    private RpcCommand setProperty(RpcContext ctx, RpcCommand request) {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        if (null == request.getBody()) {
            return response.setCodeAndRemark(ResponseCode.SUCCESS, null);
        }

        String body;
        try {
            body = new String(request.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "body is not utf-8");
        }

        Properties properties = BeanUtil.toProperties(body);
        if (properties == null) {
            return response.setCodeAndRemark(ResponseCode.SYSTEM_ERROR, "body is not properties");
        }

        if (!propertyService.validateBlackList(properties)) {
            return response.setCodeAndRemark(ResponseCode.NO_PERMISSION, "body is not allowed");
        }

        propertyService.update(properties);

        return response.success();
    }
}
