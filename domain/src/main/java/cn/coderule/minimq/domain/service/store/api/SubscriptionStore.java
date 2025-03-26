package cn.coderule.minimq.domain.service.store.api;

public interface SubscriptionStore {
    boolean existsGroup(String topicName);
    void getGroup(String groupName);
    void saveGroup();
    void deleteGroup(String groupName);
}
