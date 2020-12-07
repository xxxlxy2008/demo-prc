package com.demo.rpc.serialization;

import java.io.IOException;

/**
 * Created on 2020-06-19
 */
public interface Serialization {
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}