package com.ucar.datalink.manager.core.rest;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.io.UnsupportedEncodingException;

/**
 * Created by user on 2017/9/29.
 */
public class MyStringSerializer implements ZkSerializer {

    public Object deserialize(final byte[] bytes) throws ZkMarshallingError {
        try {
            return new String(bytes, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new ZkMarshallingError(e);
        }
    }

    public byte[] serialize(final Object data) throws ZkMarshallingError {
        try {
            return ((String) data).getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new ZkMarshallingError(e);
        }
    }

}
