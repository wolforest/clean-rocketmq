package cn.coderule.wolfmq.rpc.common.core.relay.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

    @Test
    void testConstructor() {
        int code = 200;
        String remark = "Success";
        String data = "test result";
        
        Result<String> result = new Result<>(code, remark, data);
        
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(remark, result.getRemark());
        assertEquals(data, result.getResult());
    }

    @Test
    void testSettersAndGetters() {
        Result<String> result = new Result<>(0, "", "");
        
        result.setCode(404);
        result.setRemark("Not Found");
        result.setResult("error");
        
        assertEquals(404, result.getCode());
        assertEquals("Not Found", result.getRemark());
        assertEquals("error", result.getResult());
    }

    @Test
    void testWithNullResult() {
        Result<Object> result = new Result<>(200, "OK", null);
        
        assertNull(result.getResult());
    }

    @Test
    void testWithDifferentTypes() {
        Result<Integer> intResult = new Result<>(200, "OK", 42);
        assertEquals(42, intResult.getResult());
        
        Result<Boolean> boolResult = new Result<>(200, "OK", true);
        assertTrue(boolResult.getResult());
    }
}
