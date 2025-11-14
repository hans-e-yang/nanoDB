package org.nanodb.utils;

import java.nio.ByteBuffer;
import java.util.List;

public class ByteExtensions {
    public static byte[] concat(byte[]... bytes) {
        return concat(List.of(bytes));
    }

    public static byte[] concat(List<byte[]> bytes) {
        int sum = 0;
        for (byte[] data : bytes) sum += data.length;

        ByteBuffer returnValue = ByteBuffer.allocate(sum);
        for (byte[] data : bytes) 
            returnValue.put(data);

        return returnValue.array();
    }
}
