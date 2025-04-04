package cn.coderule.minimq.rpc.registry.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.netty.NettyClient;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.TopicInfo;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultRegistryClient implements RegistryClient, Lifecycle {
    private final RpcClientConfig config;
    private final NettyClient nettyClient;
    private final HashedWheelTimer timer;

    private final AtomicReference<List<String>> addressList;
    private final AtomicReference<String> activeAddress;

    public DefaultRegistryClient(RpcClientConfig config, String addressConfig) {
        this.config = config;
        this.nettyClient = new NettyClient(config);
        this.timer = new HashedWheelTimer(r -> new Thread(r, "RegistryScanThread"));

        this.addressList = new AtomicReference<>();
        this.activeAddress = new AtomicReference<>();

        setRegistryList(addressConfig);
    }

    @Override
    public void start() {
        this.nettyClient.start();
    }

    @Override
    public void shutdown() {
        this.nettyClient.shutdown();
        this.timer.stop();
    }

    @Override
    public void setRegistryList(List<String> addrs) {
        if (CollectionUtil.isEmpty(addrs)) {
            return;
        }

        List<String> preList = this.addressList.get();
        if (!CollectionUtil.isDifferent(preList, addrs)) {
            return;
        }

        Collections.shuffle(addrs);
        this.addressList.set(addrs);
        log.info("set registry address list, pre: {}; new: {}", preList, addrs);

        closeActiveAddress(addrs);
    }

    @Override
    public void setRegistryList(String addressConfig) {
        if (StringUtil.isBlank(addressConfig)) {
            return;
        }

        String[] arr = addressConfig.split(";");
        if (arr.length == 0) {
            return;
        }
        setRegistryList(List.of(arr));
    }

    @Override
    public List<String> getRegistryList() {
        return addressList.get();
    }

    @Override
    public void registerBroker(BrokerInfo brokerInfo) {
        List<StoreRegisterResult> results = new CopyOnWriteArrayList<>();


        RegisterBrokerRequestHeader requestHeader = new RegisterBrokerRequestHeader();
    }

    @Override
    public void unregisterBroker(ServerInfo serverInfo) {

    }

    @Override
    public void brokerHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerStore(StoreInfo storeInfo) {

    }

    @Override
    public void unregisterStore(ServerInfo serverInfo) {

    }

    @Override
    public void storeHeartbeat(HeartBeat heartBeat) {

    }

    @Override
    public void registerTopic(TopicInfo topicInfo) {

    }

    @Override
    public ClusterInfo syncClusterInfo(String clusterName) {
        return null;
    }

    @Override
    public GroupInfo syncGroupInfo(String clusterName, String groupName) {
        return null;
    }

    @Override
    public RouteInfo syncRouteInfo(String topicName, long timeout) {
        return null;
    }

    private void startScanService() {
        int connectTimeout = config.getConnectTimeout();
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                try {
                    DefaultRegistryClient.this.scanAvailableRegistry();
                } catch (Throwable t) {
                    log.error("DefaultRegistryClient.scanAvailableRegistry exception", t);
                } finally {
                    timer.newTimeout(this, connectTimeout, TimeUnit.MILLISECONDS);
                }
            }
        };

        this.timer.newTimeout(task, 0, TimeUnit.MILLISECONDS);
    }

    private void scanAvailableRegistry() {

    }

    private void closeActiveAddress(List<String> addrs) {
        String activeAddr = this.activeAddress.get();
        if (null == activeAddr || addrs.contains(activeAddr)) {
            return;
        }

        nettyClient.closeChannel(activeAddr);
    }
}
