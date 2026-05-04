package cn.coderule.wolfmq.domain.domain.meta.subscription;

import cn.coderule.wolfmq.domain.domain.consumer.consume.RetryPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupRetryPolicyTest {

    @Test
    void getRetryPolicy_exponential_withNonNullPolicy_returnsIt() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        ExponentialRetryPolicy exponential = new ExponentialRetryPolicy(1000L, 60000L, 2);
        policy.setType(GroupRetryPolicyType.EXPONENTIAL);
        policy.setExponentialRetryPolicy(exponential);

        RetryPolicy result = policy.getRetryPolicy();
        assertSame(exponential, result);
    }

    @Test
    void getRetryPolicy_exponential_withNullPolicy_returnsDefault() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        policy.setType(GroupRetryPolicyType.EXPONENTIAL);
        policy.setExponentialRetryPolicy(null);

        RetryPolicy result = policy.getRetryPolicy();
        assertNotNull(result);
        assertTrue(result instanceof CustomizedRetryPolicy);
    }

    @Test
    void getRetryPolicy_customized_withNonNullPolicy_returnsIt() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        CustomizedRetryPolicy customized = new CustomizedRetryPolicy();
        policy.setType(GroupRetryPolicyType.CUSTOMIZED);
        policy.setCustomizedRetryPolicy(customized);

        RetryPolicy result = policy.getRetryPolicy();
        assertSame(customized, result);
    }

    @Test
    void getRetryPolicy_customized_withNullPolicy_returnsDefault() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        policy.setType(GroupRetryPolicyType.CUSTOMIZED);
        policy.setCustomizedRetryPolicy(null);

        RetryPolicy result = policy.getRetryPolicy();
        assertNotNull(result);
        assertTrue(result instanceof CustomizedRetryPolicy);
    }

    @Test
    void setType_changesType() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        assertEquals(GroupRetryPolicyType.CUSTOMIZED, policy.getType());

        policy.setType(GroupRetryPolicyType.EXPONENTIAL);
        assertEquals(GroupRetryPolicyType.EXPONENTIAL, policy.getType());

        policy.setType(GroupRetryPolicyType.CUSTOMIZED);
        assertEquals(GroupRetryPolicyType.CUSTOMIZED, policy.getType());
    }

    @Test
    void setExponentialRetryPolicy_works() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        ExponentialRetryPolicy exponential = new ExponentialRetryPolicy(500L, 30000L, 3);

        policy.setExponentialRetryPolicy(exponential);
        assertSame(exponential, policy.getExponentialRetryPolicy());
        assertEquals(500L, policy.getExponentialRetryPolicy().getInitial());
    }

    @Test
    void setCustomizedRetryPolicy_works() {
        GroupRetryPolicy policy = new GroupRetryPolicy();
        CustomizedRetryPolicy customized = new CustomizedRetryPolicy();

        policy.setCustomizedRetryPolicy(customized);
        assertSame(customized, policy.getCustomizedRetryPolicy());
    }
}