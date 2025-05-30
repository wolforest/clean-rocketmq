
package cn.coderule.minimq.rpc.registry.protocol.body;

import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.meta.DataVersion;
import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.minimq.domain.domain.model.meta.statictopic.TopicQueueMappingInfo;
import com.alibaba.fastjson2.JSON;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RegisterBrokerBody extends RpcSerializable {
    private static final long MINIMUM_TAKE_TIME_MILLISECOND = 50;

    private TopicConfigAndMappingSerializeWrapper topicConfigSerializeWrapper
        = new TopicConfigAndMappingSerializeWrapper();

    private List<String> filterServerList = new ArrayList<>();

    public byte[] encode(boolean compress) {

        if (!compress) {
            return super.encode();
        }
        long start = System.currentTimeMillis();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream outputStream = new DeflaterOutputStream(byteArrayOutputStream, new Deflater(Deflater.BEST_COMPRESSION));
        DataVersion dataVersion = topicConfigSerializeWrapper.getDataVersion();
        ConcurrentMap<String, Topic> topicConfigTable = cloneTopicConfigTable(topicConfigSerializeWrapper.getTopicConfigTable());
        assert topicConfigTable != null;
        try {
            byte[] buffer = RpcSerializable.encode(dataVersion);

            // write data version
            outputStream.write(convertIntToByteArray(buffer.length));
            outputStream.write(buffer);

            int topicNumber = topicConfigTable.size();

            // write number of topic configs
            outputStream.write(convertIntToByteArray(topicNumber));

            // write topic config entry one by one.
            for (ConcurrentMap.Entry<String, Topic> next : topicConfigTable.entrySet()) {
                buffer = next.getValue().encode().getBytes(MQConstants.DEFAULT_CHARSET);
                outputStream.write(convertIntToByteArray(buffer.length));
                outputStream.write(buffer);
            }

            buffer = JSON.toJSONString(filterServerList).getBytes(MQConstants.DEFAULT_CHARSET);

            // write filter server list json length
            outputStream.write(convertIntToByteArray(buffer.length));

            // write filter server list json
            outputStream.write(buffer);

            //write the topic queue mapping
            Map<String, TopicQueueMappingInfo> topicQueueMappingInfoMap = topicConfigSerializeWrapper.getTopicQueueMappingInfoMap();
            if (topicQueueMappingInfoMap == null) {
                //as the placeholder
                topicQueueMappingInfoMap = new ConcurrentHashMap<>();
            }
            outputStream.write(convertIntToByteArray(topicQueueMappingInfoMap.size()));
            for (TopicQueueMappingInfo info: topicQueueMappingInfoMap.values()) {
                buffer = JSON.toJSONString(info).getBytes(MQConstants.DEFAULT_CHARSET);
                outputStream.write(convertIntToByteArray(buffer.length));
                // write filter server list json
                outputStream.write(buffer);
            }

            outputStream.finish();
            long takeTime = System.currentTimeMillis() - start;
            if (takeTime > MINIMUM_TAKE_TIME_MILLISECOND) {
                log.info("Compressing takes {}ms", takeTime);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Failed to compress RegisterBrokerBody object", e);
        }

        return null;
    }

    public static RegisterBrokerBody decode(byte[] data, boolean compressed) throws IOException {
        if (!compressed) {
            return RegisterBrokerBody.decode(data, RegisterBrokerBody.class);
        }
        long start = System.currentTimeMillis();
        InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data));
        int dataVersionLength = readInt(inflaterInputStream);
        byte[] dataVersionBytes = readBytes(inflaterInputStream, dataVersionLength);
        DataVersion dataVersion = RpcSerializable.decode(dataVersionBytes, DataVersion.class);

        RegisterBrokerBody registerBrokerBody = new RegisterBrokerBody();
        registerBrokerBody.getTopicConfigSerializeWrapper().setDataVersion(dataVersion);
        ConcurrentMap<String, Topic> topicConfigTable = registerBrokerBody.getTopicConfigSerializeWrapper().getTopicConfigTable();

        int topicConfigNumber = readInt(inflaterInputStream);
        log.debug("{} topic configs to extract", topicConfigNumber);

        for (int i = 0; i < topicConfigNumber; i++) {
            int topicConfigJsonLength = readInt(inflaterInputStream);

            byte[] buffer = readBytes(inflaterInputStream, topicConfigJsonLength);
            Topic topicConfig = new Topic();
            String topicConfigJson = new String(buffer, MQConstants.DEFAULT_CHARSET);
            topicConfig.decode(topicConfigJson);
            topicConfigTable.put(topicConfig.getTopicName(), topicConfig);
        }

        int filterServerListJsonLength = readInt(inflaterInputStream);

        byte[] filterServerListBuffer = readBytes(inflaterInputStream, filterServerListJsonLength);
        String filterServerListJson = new String(filterServerListBuffer, MQConstants.DEFAULT_CHARSET);
        List<String> filterServerList = new ArrayList<>();
        try {
            filterServerList = JSON.parseArray(filterServerListJson, String.class);
        } catch (Exception e) {
            log.error("Decompressing occur Exception {}", filterServerListJson);
        }

        registerBrokerBody.setFilterServerList(filterServerList);

        int topicQueueMappingNum = readInt(inflaterInputStream);
        Map<String/* topic */, TopicQueueMappingInfo> topicQueueMappingInfoMap = new ConcurrentHashMap<>();
        for (int i = 0; i < topicQueueMappingNum; i++) {
            int mappingJsonLen = readInt(inflaterInputStream);
            byte[] buffer = readBytes(inflaterInputStream, mappingJsonLen);
            TopicQueueMappingInfo info = RpcSerializable.decode(buffer, TopicQueueMappingInfo.class);
            topicQueueMappingInfoMap.put(info.getTopic(), info);
        }
        registerBrokerBody.getTopicConfigSerializeWrapper().setTopicQueueMappingInfoMap(topicQueueMappingInfoMap);

        long takeTime = System.currentTimeMillis() - start;
        if (takeTime > MINIMUM_TAKE_TIME_MILLISECOND) {
            log.info("Decompressing takes {}ms", takeTime);
        }
        return registerBrokerBody;
    }

    private static byte[] convertIntToByteArray(int n) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(n);
        return byteBuffer.array();
    }

    private static byte[] readBytes(InflaterInputStream inflaterInputStream, int length) throws IOException {
        byte[] buffer = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int len = inflaterInputStream.read(buffer, bytesRead, length - bytesRead);
            if (len == -1) {
                throw new IOException("End of compressed data has reached");
            } else {
                bytesRead += len;
            }
        }
        return buffer;
    }

    private static int readInt(InflaterInputStream inflaterInputStream) throws IOException {
        byte[] buffer = readBytes(inflaterInputStream, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        return byteBuffer.getInt();
    }

    public TopicConfigAndMappingSerializeWrapper getTopicConfigSerializeWrapper() {
        return topicConfigSerializeWrapper;
    }

    public void setTopicConfigSerializeWrapper(TopicConfigAndMappingSerializeWrapper topicConfigSerializeWrapper) {
        this.topicConfigSerializeWrapper = topicConfigSerializeWrapper;
    }

    public List<String> getFilterServerList() {
        return filterServerList;
    }

    public void setFilterServerList(List<String> filterServerList) {
        this.filterServerList = filterServerList;
    }

    private ConcurrentMap<String, Topic> cloneTopicConfigTable(
        ConcurrentMap<String, Topic> topicConfigConcurrentMap) {
        if (topicConfigConcurrentMap == null) {
            return null;
        }
        ConcurrentHashMap<String, Topic> result = new ConcurrentHashMap<>(topicConfigConcurrentMap.size());
        result.putAll(topicConfigConcurrentMap);
        return result;
    }
}
