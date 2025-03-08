package cn.coderule.minimq.rpc.common.core.invoke;

public interface RpcHook {
    void beforeRequest(final RpcCommand request);
    void afterResponse(final RpcCommand request, final RpcCommand response);
}
