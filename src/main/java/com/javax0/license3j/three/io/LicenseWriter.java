package com.javax0.license3j.three.io;

import com.javax0.license3j.three.License;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LicenseWriter implements Closeable {
    private final OutputStream os;

    public LicenseWriter(OutputStream os) {
        this.os = os;
    }

    public LicenseWriter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public LicenseWriter(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    public void write(License license, IOFormat format) throws IOException {
        switch (format) {
            case BINARY:
                os.write(license.serialized());
                return;
            case BASE64:
                os.write(Base64.getEncoder().encode(license.serialized()));
                return;
            case STRING:
                os.write(license.toString().getBytes(StandardCharsets.UTF_8));
                return;
        }
        throw new IllegalArgumentException("License format " + format + " is unknown");
    }

    public void write(License license) throws IOException {
        write(license, IOFormat.BINARY);
    }

    @Override
    public void close() throws IOException {

    }
}
