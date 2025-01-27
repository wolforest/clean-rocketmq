package com.wolf.minimq.hello.java.mmap;

import com.wolf.common.util.lang.RandomUtil;
import com.wolf.common.util.test.Timer;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloMappedFile {
    public static void main(String[] args) throws IOException {
        new HelloMappedFile().execute();
    }

    public void execute() throws IOException {
        FileChannel channel = null;
        File file = null;
        MappedByteBuffer mappedByteBuffer = null;
        int count = 1000_000;
        int fileSize = 1000 * count;

        try {
            file = createTmpFile();

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            channel = randomAccessFile.getChannel();
            mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            Timer timer = new Timer();

            timer.start();
            write(mappedByteBuffer, count);
            timer.record("MappedByteBufferWrite");

            // mappedByteBuffer.force();
            // timer.record("MappedByteBufferForce");

            read(mappedByteBuffer, fileSize, count);
            timer.record("MappedByteBufferRead");

            log.info("{}", timer);
        } catch (IOException e) {
            log.info("Failed to operate channel read/write: ", e);
        } finally {
            release(mappedByteBuffer, channel, file);
        }
    }

    public void write(MappedByteBuffer mappedByteBuffer, int count) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.put("1234567890".repeat(100).getBytes(StandardCharsets.UTF_8));

        for (int i = 0; i < count; i++) {
            int position = 1000 * i;

            mappedByteBuffer.position(position);
            mappedByteBuffer.put(buffer);

            buffer.rewind();
        }
    }

    public void read(MappedByteBuffer mappedByteBuffer, int maxOffset, int count) throws IOException {
        ByteBuffer bufferResult = ByteBuffer.allocate(1000);
        maxOffset -= 2000;

        for (int i = 0; i < count; i++) {
            bufferResult.clear();
            int startOffset = RandomUtil.randomInt(maxOffset);

            mappedByteBuffer.position(startOffset);
            mappedByteBuffer.get(bufferResult.array(), 0, bufferResult.capacity());
        }

        byte[] bytes = new byte[bufferResult.limit()];
        bufferResult.get(bytes);
        log.info("read data from channel: \n{}", new String(bytes, StandardCharsets.UTF_8));
    }

    public void release(MappedByteBuffer mappedByteBuffer, FileChannel channel, File file) throws IOException {
        if (mappedByteBuffer != null) {
            mappedByteBuffer.clear();
        }

        if (channel != null) {
            channel.close();
        }
        if (file != null) {
            file.delete();
        }
    }

    private File createTmpFile() throws IOException {
        return Files.createTempFile("helloChannel", ".txt").toFile();
    }
}
