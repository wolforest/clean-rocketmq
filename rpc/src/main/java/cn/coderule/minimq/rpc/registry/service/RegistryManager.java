package cn.coderule.minimq.rpc.registry.service;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.rpc.config.RpcClientConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.NettyClient;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryManager implements Lifecycle {
    private static final long LOCK_TIMEOUT = 3000;

    private final RpcClientConfig config;
    private final NettyClient nettyClient;

    private final ExecutorService scanExecutor;
    private final HashedWheelTimer timer;

    private final AtomicReference<List<String>> addressList;
    private final AtomicReference<String> choseAddress;
    private final ConcurrentMap<String, Boolean> availableAddressMap;
    private final AtomicInteger addressIndex;

    private final Lock channelLock;

    public RegistryManager(RpcClientConfig config, String addressConfig, NettyClient nettyClient) {
        this.config = config;
        this.nettyClient = nettyClient;

        this.addressList = new AtomicReference<>();
        this.choseAddress = new AtomicReference<>();
        this.availableAddressMap = new ConcurrentHashMap<>();
        this.addressIndex = initAddressIndex();

        this.timer = new HashedWheelTimer(r -> new Thread(r, "RegistryScanTimer"));
        this.scanExecutor = initScanService();
        this.channelLock = new ReentrantLock();

        setRegistryList(addressConfig);
    }

    @Override
    public void start() {
        this.scanAvailableRegistry();
        this.startScanService();
    }

    @Override
    public void shutdown() {
        try {
            this.timer.stop();
            this.scanExecutor.shutdown();
        } catch (Exception e) {
            log.error("shutdown client error", e);
        }
    }

    public String chooseRegistry() throws InterruptedException {
        String addr = choseAddress.get();
        if (addr != null) {
            return addr;
        }

        List<String> addressList = this.addressList.get();
        if (CollectionUtil.isEmpty(addressList)) {
            return null;
        }

        if (!channelLock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
            return null;
        }

        try {
            addr = choseAddress.get();
            if (addr != null) {
                return addr;
            }

            addr = getAddressByIndex(addressList);
            this.choseAddress.set(addr);
            nettyClient.getOrCreateChannelAsync(addr);
            log.info("choose registry address: {}", addr);
            return addr;
        } catch (Exception e) {
            log.error("chooseRegistry exception", e);
        } finally {
            channelLock.unlock();
        }
        return null;
    }

    private String getAddressByIndex(List<String> addressList) {
        int index = addressIndex.incrementAndGet();
        index = Math.abs(index);
        index = index % addressList.size();

        return addressList.get(index);
    }

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

    public List<String> getRegistryList() {
        return addressList.get();
    }

    public Set<String> getAvailableRegistry() {
        return availableAddressMap.keySet();
    }

    private AtomicInteger initAddressIndex() {
        return new AtomicInteger(
            ThreadLocalRandom.current().nextInt(999)
        );
    }

    private ExecutorService initScanService() {
        return ThreadUtil.newThreadPoolExecutor(
            4,
            10,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(32),
            new DefaultThreadFactory("RegistryScanThread")
        );
    }

    private void startScanService() {
        int connectTimeout = config.getConnectTimeout();
        TimerTask task = new TimerTask() {
            @Override
            public void run(Timeout timeout) {
                try {
                    RegistryManager.this.scanAvailableRegistry(true);
                } catch (Throwable t) {
                    log.error("DefaultRegistryClient.scanAvailableRegistry exception", t);
                } finally {
                    timer.newTimeout(this, connectTimeout, TimeUnit.MILLISECONDS);
                }
            }
        };

        this.timer.newTimeout(task, connectTimeout, TimeUnit.MILLISECONDS);
    }

    private void scanAvailableRegistry() {
        scanAvailableRegistry(false);
    }

    private void scanAvailableRegistry(boolean async) {
        List<String> addressList = this.addressList.get();
        if (CollectionUtil.isEmpty(addressList)) {
            log.debug("no registry address");
            return;
        }

        removeUnavailableAddress(addressList);

        for (String address : addressList) {
            if (!async) {
                connectRegistry(address);
                continue;
            }

            scanExecutor.execute(() -> {
                connectRegistry(address);
            });
        }
    }

    private void connectRegistry(String address) {
        try {
            Channel channel = nettyClient.getOrCreateChannel(address);
            if (channel != null) {
                RegistryManager.this.availableAddressMap.putIfAbsent(address, true);
                return;
            }

            Boolean exists = RegistryManager.this.availableAddressMap.remove(address);
            if (exists != null) {
                log.warn("remove unavailable registry from availableAddressMap: {}", address);
            }
        } catch (Throwable t) {
            log.error("connect registry exception", t);
        }
    }

    private void removeUnavailableAddress(List<String> addressList) {
        for (String addr : RegistryManager.this.availableAddressMap.keySet()) {
            if (addressList.contains(addr)) {
                continue;
            }

            RegistryManager.this.availableAddressMap.remove(addr);
            log.warn("remove unavailable registry address: {}", addr);
        }
    }

    private void closeActiveAddress(List<String> addrs) {
        String activeAddr = this.choseAddress.get();
        if (null == activeAddr || addrs.contains(activeAddr)) {
            return;
        }

        nettyClient.closeChannel(activeAddr);
    }
}
