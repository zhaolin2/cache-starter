//package com.sinozo.config.protobuffer;
//
//import com.alibaba.fastjson.JSON;
//import com.github.xiaolyuh.redis.serializer.SerializationException;
//import com.sinozo.config.protobuffer.CacheWrapper;
//import com.sinozo.config.protobuffer.CustomSerializationUtils;
//import io.protostuff.LinkedBuffer;
//import io.protostuff.ProtostuffIOUtil;
//import io.protostuff.runtime.DefaultIdStrategy;
//import io.protostuff.runtime.IdStrategy;
//import io.protostuff.runtime.RuntimeSchema;
//import org.springframework.data.redis.serializer.RedisSerializer;
//
//import java.util.Arrays;
//
//public class RedisProtoBufferSerializer<T> implements RedisSerializer<T> {
//
//    IdStrategy strategy = new DefaultIdStrategy(IdStrategy.DEFAULT_FLAGS | IdStrategy.COLLECTION_SCHEMA_ON_REPEATED_FIELDS, null, 0);
//    RuntimeSchema<CacheWrapper> schema = RuntimeSchema.createFrom(CacheWrapper.class, strategy);
//
//    static {
//        System.getProperties().setProperty("protostuff.runtime.always_use_sun_reflection_factory", "true");
//        System.getProperties().setProperty("protostuff.runtime.preserve_null_elements", "true");
//        System.getProperties().setProperty("protostuff.runtime.morph_collection_interfaces", "true");
//        System.getProperties().setProperty("protostuff.runtime.morph_map_interfaces", "true");
//        System.getProperties().setProperty("protostuff.runtime.morph_non_final_pojos", "true");
//    }
//
//    @Override
//    public byte[] serialize(T t) throws SerializationException {
//        if (t == null) {
//            return CustomSerializationUtils.EMPTY_ARRAY;
//        }
//
//        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
//        try {
//            return ProtostuffIOUtil.toByteArray(new CacheWrapper<>(t), schema, buffer);
//        } catch (Exception e) {
//            throw new SerializationException(String.format("ProtostuffRedisSerializer 序列化异常: %s, [%s]", e.getMessage(), t), e);
//        } finally {
//            buffer.clear();
//        }
//
//    }
//
//    @Override
//    public T deserialize(byte[] bytes) throws SerializationException {
//        if (CustomSerializationUtils.isEmpty(bytes)) {
//            return null;
//        }
//
//        if (Arrays.equals(CustomSerializationUtils.EMPTY_ARRAY, bytes)) {
//            return null;
//        }
//
//
//        try {
//            CacheWrapper<T> wrapper = new CacheWrapper<>(null);
//            ProtostuffIOUtil.mergeFrom(bytes, wrapper, schema);
//            return wrapper.getData();
//        } catch (Exception e) {
//            throw new SerializationException(String.format("ProtostuffRedisSerializer 反序列化异常: %s, 【%s】", e.getMessage(), JSON.toJSONString(bytes)), e);
//        }
//    }
//}
