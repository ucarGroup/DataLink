package com.ucar.datalink.worker.api.util.copy;

import com.google.common.collect.Lists;
import com.ucar.datalink.contract.Record;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufException;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lubiao on 2017/3/22.
 */
@SuppressWarnings("unchecked")
public class RecordCopier {

    public static <T extends Record> T copy(T record) {
        if (record == null) {
            return null;
        }

        try {
            byte[] bytes = serialize(record);
            return (T) deserialize(bytes, record.getClass());
        } catch (Exception e) {
            throw new RecordCopyException("record copy failed.", e);
        }
    }

    public static <T extends Record> List<T> copyList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }

        List<List<T>> tempList = Lists.newArrayList();
        tempList.add(list);

        while (true) {
            try {
                List<T> result = new LinkedList<>();
                for (List<T> one : tempList) {
                    result.addAll(copyListInternal(one));
                }
                return result;
            } catch (Exception e) {
                if (e instanceof ProtobufException) {
                    if ("Protocol message was too large.  May be malicious.  Use CodedInput.setSizeLimit() to increase the size limit.".equals(e.getMessage())) {
                        //超过限制，进行一次拆分，然后重试
                        tempList = split(tempList);
                    } else {
                        throw new RecordCopyException("record list copy failed.", e);
                    }
                } else {
                    throw new RecordCopyException("record list copy failed.", e);
                }
            }
        }
    }

    private static <T> List<List<T>> split(List<List<T>> input) {
        List<List<T>> result = new LinkedList<>();
        for (List<T> one : input) {
            if (one.size() > 1) {
                int eachSize = one.size() / 2;
                List<List<T>> splitList = Lists.partition(one, eachSize);
                result.addAll(splitList);
            } else {
                result.add(one);
            }
        }
        return result;
    }

    private static <T extends Record> List<T> copyListInternal(List<T> list) throws Exception {
        byte[] bytes = serializeList(list);
        return (List<T>) deserializeList(bytes, list.get(0).getClass());
    }

    private static byte[] serialize(Object obj) {
        Schema schema = (Schema) RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        byte[] bytes = null;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    private static Object deserialize(byte[] paramArrayOfByte, Class targetClass) throws Exception {
        Object instance = targetClass.newInstance();
        Schema schema = RuntimeSchema.getSchema(targetClass);
        ProtostuffIOUtil.mergeFrom(paramArrayOfByte, instance, schema);
        return instance;
    }

    public static <T> byte[] serializeList(List<T> objList) throws Exception {
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(objList.get(0).getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024);
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            ProtostuffIOUtil.writeListTo(bos, objList, schema, buffer);
            bytes = bos.toByteArray();
        } finally {
            buffer.clear();
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public static <T> List<T> deserializeList(byte[] paramArrayOfByte, Class<T> targetClass) throws Exception {
        Schema<T> schema = RuntimeSchema.getSchema(targetClass);
        return ProtostuffIOUtil.parseListFrom(new ByteArrayInputStream(paramArrayOfByte), schema);
    }
}
