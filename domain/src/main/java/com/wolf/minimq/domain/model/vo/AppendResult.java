package com.wolf.minimq.domain.model.vo;

import com.wolf.minimq.domain.enums.AppendStatus;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppendResult implements Serializable {
    // Return code
    private AppendStatus status;
    // Where to start writing
    private long wroteOffset;
    // Write Bytes
    private int wroteBytes;
    // Message storage timestamp
    private long storeTimestamp;

    public static AppendResult success(long wroteOffset) {
        return AppendResult.builder()
            .status(AppendStatus.PUT_OK)
            .wroteOffset(wroteOffset)
            .build();
    }

    public static AppendResult failure() {
        return AppendResult.builder()
            .status(AppendStatus.UNKNOWN_ERROR)
            .build();
    }

    public static AppendResult endOfFile() {
        return AppendResult.builder()
            .status(AppendStatus.END_OF_FILE)
            .build();
    }


}
