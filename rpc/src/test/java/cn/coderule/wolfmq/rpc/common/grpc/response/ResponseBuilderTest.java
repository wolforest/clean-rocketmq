package cn.coderule.wolfmq.rpc.common.grpc.response;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.wolfmq.domain.core.enums.code.InvalidCode;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.core.exception.RpcException;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingTimeoutException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseBuilderTest {

    @Test
    void testGetInstance() {
        ResponseBuilder instance1 = ResponseBuilder.getInstance();
        ResponseBuilder instance2 = ResponseBuilder.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void testBuildStatusWithCodeAndMessage() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        Status status = builder.buildStatus(Code.OK, "Success");
        
        assertNotNull(status);
        assertEquals(Code.OK, status.getCode());
        assertEquals("Success", status.getMessage());
    }

    @Test
    void testBuildStatusWithCodeAndNullMessage() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        Status status = builder.buildStatus(Code.OK, null);
        
        assertNotNull(status);
        assertEquals(Code.OK, status.getCode());
        assertEquals(Code.OK.name(), status.getMessage());
    }

    @Test
    void testBuildStatusWithRemotingTimeoutException() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        RemotingTimeoutException exception = new RemotingTimeoutException("127.0.0.1:8080", 3000, null);
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(Code.PROXY_TIMEOUT, status.getCode());
    }

    @Test
    void testBuildStatusWithInvalidRequestException() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        InvalidRequestException exception = new InvalidRequestException(InvalidCode.BAD_REQUEST, "Bad request");
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(InvalidCode.BAD_REQUEST.getCode(), status.getCode().getNumber());
    }

    @Test
    void testBuildStatusWithInvalidParameterException() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        InvalidParameterException exception = new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "Invalid topic");
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(InvalidCode.ILLEGAL_TOPIC.getCode(), status.getCode().getNumber());
    }

    @Test
    void testBuildStatusWithRpcExceptionTopicNotExist() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        RpcException exception = new RpcException(ResponseCode.TOPIC_NOT_EXIST, "Topic not found");
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(Code.TOPIC_NOT_FOUND, status.getCode());
        assertEquals("Topic not found", status.getMessage());
    }

    @Test
    void testBuildStatusWithGenericRpcException() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        RpcException exception = new RpcException(ResponseCode.SYSTEM_ERROR, "System error");
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(Code.INTERNAL_SERVER_ERROR, status.getCode());
    }

    @Test
    void testBuildStatusWithGenericException() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        Exception exception = new RuntimeException("Unexpected error");
        
        Status status = builder.buildStatus(exception);
        
        assertNotNull(status);
        assertEquals(Code.INTERNAL_SERVER_ERROR, status.getCode());
    }

    @Test
    void testBuildStatusWithIntCode() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        Status status = builder.buildStatus(ResponseCode.SUCCESS, "OK");
        
        assertNotNull(status);
        assertEquals(Code.OK, status.getCode());
        assertEquals("OK", status.getMessage());
    }

    @Test
    void testBuildStatusWithIntCodeAndNullRemark() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        Status status = builder.buildStatus(ResponseCode.SUCCESS, null);
        
        assertNotNull(status);
        assertEquals(Code.OK, status.getCode());
        assertEquals(String.valueOf(ResponseCode.SUCCESS), status.getMessage());
    }

    @Test
    void testBuildCodeWithMappedCode() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        assertEquals(Code.OK, builder.buildCode(ResponseCode.SUCCESS));
        assertEquals(Code.TOO_MANY_REQUESTS, builder.buildCode(ResponseCode.SYSTEM_BUSY));
        assertEquals(Code.NOT_IMPLEMENTED, builder.buildCode(ResponseCode.REQUEST_CODE_NOT_SUPPORTED));
        assertEquals(Code.CONSUMER_GROUP_NOT_FOUND, builder.buildCode(ResponseCode.SUBSCRIPTION_GROUP_NOT_EXIST));
    }

    @Test
    void testBuildCodeWithUnmappedCode() {
        ResponseBuilder builder = ResponseBuilder.getInstance();
        
        Code code = builder.buildCode(99999);
        
        assertEquals(Code.INTERNAL_SERVER_ERROR, code);
    }
}
