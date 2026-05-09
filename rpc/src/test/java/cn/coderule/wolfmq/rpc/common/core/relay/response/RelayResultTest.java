package cn.coderule.wolfmq.rpc.common.core.relay.response;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class RelayResultTest {

    @Test
    void testConstructor() {
        String processResult = "processed";
        CompletableFuture<Result<String>> future = CompletableFuture.completedFuture(new Result<>(200, "OK", "relay"));
        
        RelayResult<String, String> relayResult = new RelayResult<>(processResult, future);
        
        assertNotNull(relayResult);
        assertEquals(processResult, relayResult.getProcessResult());
        assertSame(future, relayResult.getRelayFuture());
    }

    @Test
    void testSettersAndGetters() {
        RelayResult<String, String> relayResult = new RelayResult<>("initial", CompletableFuture.completedFuture(null));
        
        String newProcessResult = "new processed";
        CompletableFuture<Result<String>> newFuture = CompletableFuture.completedFuture(new Result<>(201, "Created", "new"));
        
        relayResult.setProcessResult(newProcessResult);
        relayResult.setRelayFuture(newFuture);
        
        assertEquals(newProcessResult, relayResult.getProcessResult());
        assertSame(newFuture, relayResult.getRelayFuture());
    }

    @Test
    void testWithNullProcessResult() {
        CompletableFuture<Result<String>> future = CompletableFuture.completedFuture(new Result<>(200, "OK", "relay"));
        
        RelayResult<Object, String> relayResult = new RelayResult<>(null, future);
        
        assertNull(relayResult.getProcessResult());
    }

    @Test
    void testWithNullFuture() {
        RelayResult<String, Object> relayResult = new RelayResult<>("processed", null);
        
        assertNull(relayResult.getRelayFuture());
    }

    @Test
    void testCompletableFutureExecution() throws Exception {
        Result<String> expectedResult = new Result<>(200, "Success", "data");
        CompletableFuture<Result<String>> future = CompletableFuture.completedFuture(expectedResult);
        
        RelayResult<String, String> relayResult = new RelayResult<>("processed", future);
        
        Result<String> actualResult = relayResult.getRelayFuture().get();
        assertEquals(expectedResult.getCode(), actualResult.getCode());
        assertEquals(expectedResult.getRemark(), actualResult.getRemark());
        assertEquals(expectedResult.getResult(), actualResult.getResult());
    }
}
