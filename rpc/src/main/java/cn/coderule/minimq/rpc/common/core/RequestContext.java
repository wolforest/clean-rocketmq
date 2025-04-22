package cn.coderule.minimq.rpc.common.core;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;

public class RequestContext {
    public static final String REMOTE_ADDRESS = "remote-address";
    public static final String LOCAL_ADDRESS = "local-address";
    public static final String CLIENT_ID = "client-id";
    public static final String CHANNEL = "channel";
    public static final String LANGUAGE = "language";
    public static final String CLIENT_VERSION = "client-version";
    public static final String SERVER_PORT = "server-port";
    /**
     * polling timeout related parameter
     * set by (grpc)bootstrap.getDeadline().timeRemaining()
     */
    public static final String REMAINING_MS = "remaining-ms";
    public static final String ACTION = "action";
    public static final String PROTOCOL_TYPE = "protocol-type";
    public static final String NAMESPACE = "namespace";

    public static final String INNER_ACTION_PREFIX = "Inner";
    private final Map<String, Object> map = new HashMap<>();

    public static RequestContext create() {
        return new RequestContext();
    }

    public static RequestContext createForInner(String actionName) {
        return create().setAction(INNER_ACTION_PREFIX + actionName);
    }

    public static RequestContext createForInner(Class<?> clazz) {
        return createForInner(clazz.getSimpleName());
    }

    public Map<String, Object> getMap() {
        return this.map;
    }

    public RequestContext set(String key, Object val) {
        this.map.put(key, val);
        return this;
    }

    public <T> T get(String key) {
        //@SuppressWarnings("unchecked")
        return (T) this.map.get(key);
    }

    public RequestContext setServerPort(int port) {
        this.set(SERVER_PORT, port);
        return this;
    }

    public Integer getServerPort() {
        return this.get(SERVER_PORT);
    }

    public RequestContext setLocalAddress(String localAddress) {
        this.set(LOCAL_ADDRESS, localAddress);
        return this;
    }

    public String getLocalAddress() {
        return this.get(LOCAL_ADDRESS);
    }

    public RequestContext setRemoteAddress(String remoteAddress) {
        this.set(REMOTE_ADDRESS, remoteAddress);
        return this;
    }

    public String getRemoteAddress() {
        return this.get(REMOTE_ADDRESS);
    }

    public RequestContext setClientID(String clientID) {
        this.set(CLIENT_ID, clientID);
        return this;
    }

    public String getClientID() {
        return this.get(CLIENT_ID);
    }

    public RequestContext setChannel(Channel channel) {
        this.set(CHANNEL, channel);
        return this;
    }

    public Channel getChannel() {
        return this.get(CHANNEL);
    }

    public RequestContext setLanguage(String language) {
        this.set(LANGUAGE, language);
        return this;
    }

    public String getLanguage() {
        return this.get(LANGUAGE);
    }

    public RequestContext setClientVersion(String clientVersion) {
        this.set(CLIENT_VERSION, clientVersion);
        return this;
    }

    public String getClientVersion() {
        return this.get(CLIENT_VERSION);
    }

    public RequestContext setRemainingMs(Long remainingMs) {
        this.set(REMAINING_MS, remainingMs);
        return this;
    }

    public Long getRemainingMs() {
        return this.get(REMAINING_MS);
    }

    public RequestContext setAction(String action) {
        this.set(ACTION, action);
        return this;
    }

    public String getAction() {
        return this.get(ACTION);
    }

    public RequestContext setProtocolType(String protocol) {
        this.set(PROTOCOL_TYPE, protocol);
        return this;
    }

    public String getProtocolType() {
        return this.get(PROTOCOL_TYPE);
    }

    public RequestContext setNamespace(String namespace) {
        this.set(NAMESPACE, namespace);
        return this;
    }

    public String getNamespace() {
        return this.get(NAMESPACE);
    }

}
