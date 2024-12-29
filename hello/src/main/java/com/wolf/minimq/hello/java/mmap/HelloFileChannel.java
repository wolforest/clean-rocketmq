package com.wolf.minimq.hello.java.mmap;

import com.wolf.common.util.lang.RandomUtil;
import com.wolf.common.util.time.Timer;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloFileChannel {
    public static void main(String[] args) throws IOException {
        new HelloFileChannel().execute();
    }

    public void execute() throws IOException {
        FileChannel channel = null;
        File file = null;
        int count = 1000_000;
        try {
            file = createTmpFile();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            channel = randomAccessFile.getChannel();
            Timer timer = new Timer();

            timer.start();
            write(channel, count);
            timer.record("FileChannelWrite");

            long maxOffset = channel.size();
            read(channel, maxOffset, count);
            timer.record("FileChannelRead");

            log.info("{}", timer);
        } catch (IOException e) {
            log.info("Failed to operate channel read/write: ", e);
        } finally {
            release(channel, file);
        }
    } public void release(FileChannel channel, File file) throws IOException {
        if (channel != null) {
            channel.close();
        }
        if (file != null) {
            file.delete();
        }
    }

    public void write(FileChannel channel, int count) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.put("1234567890".repeat(100).getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        for (int i = 0; i < count; i++) {
            buffer.rewind();
            channel.write(buffer);
        }
    }

    public void read(FileChannel channel, long maxOffset, int count) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        maxOffset -= 2000;

        for (int i = 0; i < count; i++) {
            buffer.clear();
            long startOffset = RandomUtil.randomLong(maxOffset);
            channel.read(buffer, startOffset);
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        log.info("read data from channel: \n{}", new String(bytes, StandardCharsets.UTF_8));
    }

    private File createTmpFile() throws IOException {
        return Files.createTempFile("helloChannel", ".txt").toFile();
    }
}
