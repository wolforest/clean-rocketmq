package com.wolf.minimq.broker.server.vo;

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
    /**
     * polling timeout related parameter
     * set by (grpc)context.getDeadline().timeRemaining()
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

    public RequestContext withVal(String key, Object val) {
        this.map.put(key, val);
        return this;
    }

    public <T> T getVal(String key) {
        return (T) this.map.get(key);
    }

    public RequestContext setLocalAddress(String localAddress) {
        this.withVal(LOCAL_ADDRESS, localAddress);
        return this;
    }

    public String getLocalAddress() {
        return this.getVal(LOCAL_ADDRESS);
    }

    public RequestContext setRemoteAddress(String remoteAddress) {
        this.withVal(REMOTE_ADDRESS, remoteAddress);
        return this;
    }

    public String getRemoteAddress() {
        return this.getVal(REMOTE_ADDRESS);
    }

    public RequestContext setClientID(String clientID) {
        this.withVal(CLIENT_ID, clientID);
        return this;
    }

    public String getClientID() {
        return this.getVal(CLIENT_ID);
    }

    public RequestContext setChannel(Channel channel) {
        this.withVal(CHANNEL, channel);
        return this;
    }

    public Channel getChannel() {
        return this.getVal(CHANNEL);
    }

    public RequestContext setLanguage(String language) {
        this.withVal(LANGUAGE, language);
        return this;
    }

    public String getLanguage() {
        return this.getVal(LANGUAGE);
    }

    public RequestContext setClientVersion(String clientVersion) {
        this.withVal(CLIENT_VERSION, clientVersion);
        return this;
    }

    public String getClientVersion() {
        return this.getVal(CLIENT_VERSION);
    }

    public RequestContext setRemainingMs(Long remainingMs) {
        this.withVal(REMAINING_MS, remainingMs);
        return this;
    }

    public Long getRemainingMs() {
        return this.getVal(REMAINING_MS);
    }

    public RequestContext setAction(String action) {
        this.withVal(ACTION, action);
        return this;
    }

    public String getAction() {
        return this.getVal(ACTION);
    }

    public RequestContext setProtocolType(String protocol) {
        this.withVal(PROTOCOL_TYPE, protocol);
        return this;
    }

    public String getProtocolType() {
        return this.getVal(PROTOCOL_TYPE);
    }

    public RequestContext setNamespace(String namespace) {
        this.withVal(NAMESPACE, namespace);
        return this;
    }

    public String getNamespace() {
        return this.getVal(NAMESPACE);
    }

}
