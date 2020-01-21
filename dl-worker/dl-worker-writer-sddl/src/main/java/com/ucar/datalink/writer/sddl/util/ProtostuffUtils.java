package com.ucar.datalink.writer.sddl.util;

import com.google.common.collect.Maps;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: duplicator (Collection...)
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 13/03/2018.
 */
public class ProtostuffUtils {

    private static Logger logger = LoggerFactory.getLogger(ProtostuffUtils.class);

    /**
     * 需要使用包装类进行序列化/反序列化的class集合
     */
    private static final Set<Class<?>> WRAPPER_SET = new HashSet<>();

    /**
     * 序列化/反序列化包装类 Class 对象
     */
    private static final Class<SerializeDeserializeWrapper> WRAPPER_CLASS = SerializeDeserializeWrapper.class;

    /**
     * 序列化/反序列化包装类 Schema 对象
     */
    private static final Schema<SerializeDeserializeWrapper> WRAPPER_SCHEMA = RuntimeSchema.createFrom(WRAPPER_CLASS);

    /**
     * 缓存对象及对象schema信息集合
     */
    private static final Map<Class<?>, Schema<?>> CACHE_SCHEMA = Maps.newConcurrentMap();

    /**
     * 预定义一些Protostuff无法直接序列化/反序列化的对象
     */
    static {
        WRAPPER_SET.add(List.class);
        WRAPPER_SET.add(ArrayList.class);
        WRAPPER_SET.add(CopyOnWriteArrayList.class);
        WRAPPER_SET.add(LinkedList.class);
        WRAPPER_SET.add(Stack.class);
        WRAPPER_SET.add(Vector.class);

        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(HashMap.class);
        WRAPPER_SET.add(ConcurrentHashMap.class);
        WRAPPER_SET.add(TreeMap.class);
        WRAPPER_SET.add(Hashtable.class);
        WRAPPER_SET.add(SortedMap.class);
        WRAPPER_SET.add(Map.class);

        WRAPPER_SET.add(Object.class);
    }

    /**
     * 注册需要使用包装类进行序列化/反序列化的 Class 对象
     *
     * @param clazz 需要包装的类型 Class 对象
     */
    public static void registerWrapperClass(Class clazz) {
        WRAPPER_SET.add(clazz);
    }

    public static <T> T coper (T  cls) {
        byte[] serializeBytes = serialize(cls);
        return deserialize(serializeBytes, (Class<T>) cls.getClass());

    }

    /**
     * 获取序列化对象类型的schema
     *
     * @param cls 序列化对象的class
     * @param <T> 序列化对象的类型
     * @return 序列化对象类型的schema
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) CACHE_SCHEMA.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            CACHE_SCHEMA.put(cls, schema);
        }
        return schema;
    }

    /**
     * 序列化对象
     *
     * @param obj 需要序列化的对象
     * @param <T> 序列化对象的类型
     * @return 序列化后的二进制数组
     */
    @SuppressWarnings("unchecked")
    private static <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Object serializeObject = obj;
            Schema schema = WRAPPER_SCHEMA;
            if (!WRAPPER_SET.contains(clazz)) {
                schema = getSchema(clazz);
            } else {
                serializeObject = SerializeDeserializeWrapper.builder(obj);
            }
            return ProtostuffIOUtil.toByteArray(serializeObject, schema, buffer);
        } catch (Exception e) {
            logger.error("序列化对象异常 [" + obj + "]", e);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化对象
     *
     * @param data  需要反序列化的二进制数组
     * @param clazz 反序列化后的对象class
     * @param <T>   反序列化后的对象类型
     * @return 反序列化后的对象集合
     */
    private static <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            if (!WRAPPER_SET.contains(clazz)) {
                T message = clazz.newInstance();
                Schema<T> schema = getSchema(clazz);
                ProtostuffIOUtil.mergeFrom(data, message, schema);
                return message;
            } else {
                SerializeDeserializeWrapper<T> wrapper = new SerializeDeserializeWrapper<>();
                ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
                return wrapper.getData();
            }
        } catch (Exception e) {
            logger.error("反序列化对象异常 [" + clazz.getName() + "]", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * @Description:
     * @Author : yongwang.chen@ucarinc.com
     * @Created: 13/03/2018.
     */
    static class SerializeDeserializeWrapper<T> {

        private T data;

        public static <T> SerializeDeserializeWrapper<T> builder(T data) {
            SerializeDeserializeWrapper<T> wrapper = new SerializeDeserializeWrapper<>();
            wrapper.setData(data);
            return wrapper;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
