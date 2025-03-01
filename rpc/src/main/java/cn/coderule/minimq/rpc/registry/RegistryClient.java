package cn.coderule.minimq.rpc.registry;

import cn.coderule.minimq.rpc.common.RpcClient;
import java.util.List;

public interface RegistryClient {
    void setRpcClient(RpcClient rpcClient);

    List<String> getServerList();
    void setServerList(List<String> serverList);
    void scanServerList();


}
