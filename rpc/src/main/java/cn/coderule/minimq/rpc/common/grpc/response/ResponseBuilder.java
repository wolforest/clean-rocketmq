
package cn.coderule.minimq.rpc.common.grpc.response;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.common.util.lang.bean.ExceptionUtil;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingTimeoutException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseBuilder {
    protected static final Map<Integer, Code> RESPONSE_CODE_MAPPING = new ConcurrentHashMap<>();

    protected static final Object INSTANCE_CREATE_LOCK = new Object();
    protected static volatile ResponseBuilder instance;

    static {
        RESPONSE_CODE_MAPPING.put(ResponseCode.SUCCESS, Code.OK);
        RESPONSE_CODE_MAPPING.put(ResponseCode.SYSTEM_BUSY, Code.TOO_MANY_REQUESTS);
        RESPONSE_CODE_MAPPING.put(ResponseCode.REQUEST_CODE_NOT_SUPPORTED, Code.NOT_IMPLEMENTED);
        RESPONSE_CODE_MAPPING.put(ResponseCode.SUBSCRIPTION_GROUP_NOT_EXIST, Code.CONSUMER_GROUP_NOT_FOUND);
    }

    public static ResponseBuilder getInstance() {
        if (instance != null) {
            return instance;
        }

        synchronized (INSTANCE_CREATE_LOCK) {
            if (instance == null) {
                instance = new ResponseBuilder();
            }
        }
        return instance;
    }

    public Status buildStatus(Code code, String message) {
        return Status.newBuilder()
            .setCode(code)
            .setMessage(message != null ? message : code.name())
            .build();
    }

    public Status buildStatus(Throwable t) {
        t = ExceptionUtil.getRealException(t);

        if (t instanceof RemotingTimeoutException) {
            return buildStatus(Code.PROXY_TIMEOUT, t.getMessage());
        }

        if (t instanceof InvalidRequestException grpcException) {
            int intCode = grpcException.getInvalidCode().getCode();
            Code code = Code.forNumber(intCode);
            return buildStatus(code, grpcException.getMessage());
        }

        if (t instanceof InvalidParameterException parameterException) {
            int intCode = parameterException.getInvalidCode().getCode();
            Code code = Code.forNumber(intCode);
            return buildStatus(code, parameterException.getMessage());
        }

        if (t instanceof cn.coderule.minimq.domain.core.exception.RpcException rpcException) {
            if (ResponseCode.TOPIC_NOT_EXIST == rpcException.getCode()) {
                return buildStatus(Code.TOPIC_NOT_FOUND, rpcException.getMessage());
            }

            int code = (int)rpcException.getCode();
            return buildStatus(buildCode(code), rpcException.getMessage());
        }

        log.error("internal server error", t);
        return buildStatus(Code.INTERNAL_SERVER_ERROR, ExceptionUtil.getErrorMessage(t));
    }

    public Status buildStatus(int code, String remark) {
        String message = remark;
        if (message == null) {
            message = String.valueOf(code);
        }
        return Status.newBuilder()
            .setCode(buildCode(code))
            .setMessage(message)
            .build();
    }

    public Code buildCode(int remotingResponseCode) {
        return RESPONSE_CODE_MAPPING.getOrDefault(remotingResponseCode, Code.INTERNAL_SERVER_ERROR);
    }
}
