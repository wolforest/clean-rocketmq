package cn.coderule.wolfmq.domain.core.constant.flag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageSysFlagTest {

    @Test
    void getTransactionType_normalMessage() {
        assertEquals(MessageSysFlag.NORMAL_MESSAGE,
            MessageSysFlag.getTransactionType(MessageSysFlag.NORMAL_MESSAGE));
    }

    @Test
    void getTransactionType_prepareMessage() {
        assertEquals(MessageSysFlag.PREPARE_MESSAGE,
            MessageSysFlag.getTransactionType(MessageSysFlag.PREPARE_MESSAGE));
    }

    @Test
    void getTransactionType_commitMessage() {
        assertEquals(MessageSysFlag.COMMIT_MESSAGE,
            MessageSysFlag.getTransactionType(MessageSysFlag.COMMIT_MESSAGE));
    }

    @Test
    void getTransactionType_rollbackMessage() {
        assertEquals(MessageSysFlag.ROLLBACK_MESSAGE,
            MessageSysFlag.getTransactionType(MessageSysFlag.ROLLBACK_MESSAGE));
    }

    @Test
    void getTransactionType_withCombinedFlags() {
        int combined = MessageSysFlag.COMPRESSED_FLAG | MessageSysFlag.COMMIT_MESSAGE;
        assertEquals(MessageSysFlag.COMMIT_MESSAGE,
            MessageSysFlag.getTransactionType(combined));
    }

    @Test
    void getTransactionType_withMultiTagsAndRollback() {
        int combined = MessageSysFlag.MULTI_TAGS_FLAG | MessageSysFlag.ROLLBACK_MESSAGE;
        assertEquals(MessageSysFlag.ROLLBACK_MESSAGE,
            MessageSysFlag.getTransactionType(combined));
    }

    @Test
    void resetTransactionType_fromCommitToRollback() {
        int result = MessageSysFlag.resetTransactionType(
            MessageSysFlag.COMMIT_MESSAGE, MessageSysFlag.ROLLBACK_MESSAGE);
        assertEquals(MessageSysFlag.ROLLBACK_MESSAGE, result);
    }

    @Test
    void resetTransactionType_fromRollbackToCommit() {
        int result = MessageSysFlag.resetTransactionType(
            MessageSysFlag.ROLLBACK_MESSAGE, MessageSysFlag.COMMIT_MESSAGE);
        assertEquals(MessageSysFlag.COMMIT_MESSAGE, result);
    }

    @Test
    void resetTransactionType_preservesNonTransactionBits() {
        int flag = MessageSysFlag.COMPRESSED_FLAG | MessageSysFlag.MULTI_TAGS_FLAG | MessageSysFlag.COMMIT_MESSAGE;
        int result = MessageSysFlag.resetTransactionType(flag, MessageSysFlag.ROLLBACK_MESSAGE);
        assertEquals(MessageSysFlag.COMPRESSED_FLAG | MessageSysFlag.MULTI_TAGS_FLAG | MessageSysFlag.ROLLBACK_MESSAGE, result);
        assertTrue(MessageSysFlag.check(result, MessageSysFlag.COMPRESSED_FLAG));
        assertTrue(MessageSysFlag.check(result, MessageSysFlag.MULTI_TAGS_FLAG));
    }

    @Test
    void resetTransactionType_fromNormalToPrepare() {
        int result = MessageSysFlag.resetTransactionType(
            MessageSysFlag.NORMAL_MESSAGE, MessageSysFlag.PREPARE_MESSAGE);
        assertEquals(MessageSysFlag.PREPARE_MESSAGE, result);
    }

    @Test
    void check_returnsTrueWhenFlagHasExpected() {
        int flag = MessageSysFlag.COMPRESSED_FLAG | MessageSysFlag.MULTI_TAGS_FLAG;
        assertTrue(MessageSysFlag.check(flag, MessageSysFlag.COMPRESSED_FLAG));
        assertTrue(MessageSysFlag.check(flag, MessageSysFlag.MULTI_TAGS_FLAG));
    }

    @Test
    void check_returnsFalseWhenFlagDoesNotHaveExpected() {
        int flag = MessageSysFlag.COMPRESSED_FLAG;
        assertFalse(MessageSysFlag.check(flag, MessageSysFlag.MULTI_TAGS_FLAG));
        assertFalse(MessageSysFlag.check(flag, MessageSysFlag.COMMIT_MESSAGE));
    }

    @Test
    void check_withInnerBatchFlag() {
        int flag = MessageSysFlag.INNER_BATCH_FLAG;
        assertTrue(MessageSysFlag.check(flag, MessageSysFlag.INNER_BATCH_FLAG));
        assertFalse(MessageSysFlag.check(flag, MessageSysFlag.COMPRESSED_FLAG));
    }

    @Test
    void check_withCompressedFlag() {
        int flag = MessageSysFlag.COMPRESSED_FLAG;
        assertTrue(MessageSysFlag.check(flag, MessageSysFlag.COMPRESSED_FLAG));
    }

    @Test
    void check_withMultiTagsFlag() {
        int flag = MessageSysFlag.MULTI_TAGS_FLAG;
        assertTrue(MessageSysFlag.check(flag, MessageSysFlag.MULTI_TAGS_FLAG));
    }

    @Test
    void check_withZeroFlag() {
        assertFalse(MessageSysFlag.check(0, MessageSysFlag.COMPRESSED_FLAG));
        assertFalse(MessageSysFlag.check(0, MessageSysFlag.MULTI_TAGS_FLAG));
    }
}