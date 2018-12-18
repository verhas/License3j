package com.javax0.license3j.three.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class ByteArrayReader {
    static byte[] readInput(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int len;
        final var data = new byte[4096];
        while ((len = is.read(data)) != -1) {
            buffer.write(data, 0, len);
        }
        return buffer.toByteArray();
    }
}
