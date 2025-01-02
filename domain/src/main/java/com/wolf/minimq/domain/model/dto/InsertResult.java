package com.wolf.minimq.domain.model.dto;

import com.wolf.minimq.domain.enums.InsertStatus;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsertResult implements Serializable {
    // Return code
    private InsertStatus status;
    // Where to start writing
    private long wroteOffset;
    // Write Bytes
    private int wroteBytes;
    // Message storage timestamp
    private long storeTimestamp;

    public boolean isSuccess() {
        return InsertStatus.PUT_OK.equals(status);
    }

    public static InsertResult success(long wroteOffset) {
        return InsertResult.builder()
            .status(InsertStatus.PUT_OK)
            .wroteOffset(wroteOffset)
            .build();
    }

    public static InsertResult failure() {
        return InsertResult.builder()
            .status(InsertStatus.UNKNOWN_ERROR)
            .build();
    }

    public static InsertResult endOfFile() {
        return InsertResult.builder()
            .status(InsertStatus.END_OF_FILE)
            .build();
    }


}
