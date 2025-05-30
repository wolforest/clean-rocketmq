package cn.coderule.minimq.store.infra.memory;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.ByteUtil;
import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransientPool implements Lifecycle {
    private final int poolSize;
    private final int fileSize;
    private final Deque<ByteBuffer> availableBuffers;
    @Getter @Setter
    private volatile boolean isRealCommit = true;

    public static void main(String[] args) {
    }

    public TransientPool(final int poolSize, final int fileSize) {
        this.poolSize = poolSize;
        this.fileSize = fileSize;
        this.availableBuffers = new ConcurrentLinkedDeque<>();
    }

    /**
     * It's a heavy init method.
     */
    @Override
    public void start() {
        for (int i = 0; i < poolSize; i++) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileSize);

            long address = ByteUtil.directBufferAddress(byteBuffer);
            Pointer pointer = new Pointer(address);
            CLibrary.INSTANCE.mlock(pointer, new NativeLong(fileSize));

            availableBuffers.offer(byteBuffer);
        }
    }

    @Override
    public void shutdown() {
        for (ByteBuffer byteBuffer : availableBuffers) {
            long address = ByteUtil.directBufferAddress(byteBuffer);
            Pointer pointer = new Pointer(address);
            CLibrary.INSTANCE.munlock(pointer, new NativeLong(fileSize));
        }
    }

    public void returnBuffer(ByteBuffer byteBuffer) {
        byteBuffer.position(0);
        byteBuffer.limit(fileSize);
        this.availableBuffers.offerFirst(byteBuffer);
    }

    public ByteBuffer borrowBuffer() {
        ByteBuffer buffer = availableBuffers.pollFirst();
        if (availableBuffers.size() < poolSize * 0.4) {
            log.warn("TransientStorePool only remain {} sheets.", availableBuffers.size());
        }
        return buffer;
    }

    public int availableBufferNums() {
        return availableBuffers.size();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
