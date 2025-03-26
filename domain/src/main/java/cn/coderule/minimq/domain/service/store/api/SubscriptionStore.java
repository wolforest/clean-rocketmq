package cn.coderule.minimq.domain.service.store.api;

public interface SubscriptionStore {
    boolean exists(String topicName);
    void get(String topicName);
    void save();
    void delete(String groupName);
}
