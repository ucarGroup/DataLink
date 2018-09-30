package com.ucar.datalink.manager.core.server;

import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.requests.AbstractRequestResponse;
import org.apache.kafka.common.requests.ResponseHeader;

import java.nio.ByteBuffer;

/**
 * Created by lubiao on 2016/12/5.
 */
class Response {
    private ByteBuffer[] buffers;

    Response(ResponseHeader header, Struct body) {
        this.buffers = sizeDelimit(serialize(header, body));
    }

    Response(ResponseHeader header, AbstractRequestResponse response) {
        this(header, response.toStruct());
    }

    ByteBuffer[] getBuffers() {
        return buffers;
    }

    private ByteBuffer serialize(ResponseHeader header, Struct body) {
        ByteBuffer buffer = ByteBuffer.allocate(header.sizeOf() + body.sizeOf());
        header.writeTo(buffer);
        body.writeTo(buffer);
        buffer.rewind();
        return buffer;
    }

    private ByteBuffer[] sizeDelimit(ByteBuffer... buffers) {
        int size = 0;
        for (int i = 0; i < buffers.length; i++)
            size += buffers[i].remaining();
        ByteBuffer[] delimited = new ByteBuffer[buffers.length + 1];
        delimited[0] = ByteBuffer.allocate(4);
        delimited[0].putInt(size);
        delimited[0].rewind();
        System.arraycopy(buffers, 0, delimited, 1, buffers.length);
        return delimited;
    }
}
