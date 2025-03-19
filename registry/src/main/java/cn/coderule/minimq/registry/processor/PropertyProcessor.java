package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.lang.BeanUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.registry.domain.property.PropertyService;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyProcessor implements RpcProcessor {
    private final PropertyService propertyService;

    public PropertyProcessor(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_NAMESRV_CONFIG -> this.setProperty(ctx, request);
            case RequestCode.GET_NAMESRV_CONFIG -> this.getProperty(ctx, request);
            default -> {
                String error = " request type " + request.getCode() + " not supported";
                yield RpcCommand.createResponseCommand(SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
            }
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

        return response.setCodeAndRemark(SystemResponseCode.SUCCESS, null);
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

        return response.setCodeAndRemark(SystemResponseCode.SUCCESS, null);
    }
}
