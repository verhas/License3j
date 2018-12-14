package com.javax0.license3j.three;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

public class Feature {
    private static final int VARIABLE_LENGTH = -1;
    private final String name;
    private final Type type;
    private final byte[] value;

    private Feature(String name, Type type, byte[] value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public byte[] serialized() {
        final var nameBuffer = name.getBytes(StandardCharsets.UTF_8);
        final var typeLength = Integer.BYTES;
        final var nameLength = Integer.BYTES + nameBuffer.length;
        final var valueLength = Integer.BYTES + value.length;
        return ByteBuffer.allocate(typeLength + nameLength + valueLength)
                .putInt(type.serialized)
                .putInt(nameBuffer.length)
                .putInt(value.length)
                .put(nameBuffer)
                .put(value)
                .array();
    }

    public boolean isBinary() {
        return type == Type.BINARY;
    }

    public boolean isString() {
        return type == Type.STRING;
    }

    public boolean isByte() {
        return type == Type.BYTE;
    }

    public boolean isShort() {
        return type == Type.SHORT;
    }

    public boolean isInteger() {
        return type == Type.INTEGER;
    }

    public boolean isLong() {
        return type == Type.LONG;
    }

    public boolean isFloat() {
        return type == Type.FLOAT;
    }

    public boolean isDouble() {
        return type == Type.DOUBLE;
    }

    public boolean isBigInteger() {
        return type == Type.BIGINTEGER;
    }

    public boolean isBigDecimal() {
        return type == Type.BIGDECIMAL;
    }

    public boolean isDate() {
        return type == Type.DATE;
    }

    public byte[] getBinary() {
        if (type != Type.BINARY) {
            throw new IllegalArgumentException("Feature is not BINARY");
        }
        return value;
    }

    public String getString() {
        if (type != Type.STRING) {
            throw new IllegalArgumentException("Feature is not STRING");
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public byte getByte() {
        if (type != Type.BYTE) {
            throw new IllegalArgumentException("Feature is not BYTE");
        }
        return value[0];
    }

    public short getShort() {
        if (type != Type.SHORT) {
            throw new IllegalArgumentException("Feature is not SHORT");
        }
        return ByteBuffer.wrap(value).getShort();
    }

    public int getInt() {
        if (type != Type.INTEGER) {
            throw new IllegalArgumentException("Feature is not INTEGER");
        }
        return ByteBuffer.wrap(value).getInt();
    }

    public long getLong() {
        if (type != Type.LONG) {
            throw new IllegalArgumentException("Feature is not LONG");
        }
        return ByteBuffer.wrap(value).getLong();
    }

    public float getFloat() {
        if (type != Type.FLOAT) {
            throw new IllegalArgumentException("Feature is not FLOAT");
        }
        return ByteBuffer.wrap(value).getFloat();
    }

    public double getDouble() {
        if (type != Type.DOUBLE) {
            throw new IllegalArgumentException("Feature is not DOUBLE");
        }
        return ByteBuffer.wrap(value).getDouble();
    }

    public BigInteger getBigInteger() {
        if (type != Type.BIGINTEGER) {
            throw new IllegalArgumentException("Feature is not BIGINTEGER");
        }
        return new BigInteger(value);
    }

    public BigDecimal getBigDecimal() {
        if (type != Type.BIGDECIMAL) {
            throw new IllegalArgumentException("Feature is not BIGDECIMAL");
        }
        var bb = ByteBuffer.wrap(value);
        var scale = bb.getInt(value.length - Integer.BYTES);

        return new BigDecimal(new BigInteger(Arrays.copyOf(value, value.length - Integer.BYTES)), scale);
    }

    public Date getDate() {
        if (type != Type.DATE) {
            throw new IllegalArgumentException("Feature is not DATE");
        }
        return new Date(ByteBuffer.wrap(value).getLong());
    }

    public enum Type {
        BINARY(1, VARIABLE_LENGTH),
        STRING(2, VARIABLE_LENGTH),
        BYTE(3, Byte.BYTES),
        SHORT(4, Short.BYTES),
        INTEGER(5, Integer.BYTES), LONG(6, Long.BYTES),
        FLOAT(7, Float.BYTES), DOUBLE(8, Double.BYTES),

        BIGINTEGER(9, VARIABLE_LENGTH), BIGDECIMAL(10, VARIABLE_LENGTH),

        DATE(11, Long.BYTES);

        final int fixedSize;
        final int serialized;

        Type(int serialized, int fixedSize) {
            this.serialized = serialized;
            this.fixedSize = fixedSize;
        }
    }

    public static class Create {
        private static void notNull(Object value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot create a feature from null value.");
            }
        }

        public static Feature binaryFeature(String name, byte[] value) {
            notNull(value);
            return new Feature(name, Type.BINARY, value);
        }

        public static Feature stringFeature(String name, String value) {
            notNull(value);
            return new Feature(name, Type.STRING, value.getBytes(StandardCharsets.UTF_8));
        }

        public static Feature byteFeature(String name, Byte value) {
            notNull(value);
            return new Feature(name, Type.BYTE, new byte[]{value});
        }

        public static Feature shortFeature(String name, Short value) {
            notNull(value);
            return new Feature(name, Type.SHORT, ByteBuffer.allocate(Short.BYTES).putShort(value).array());
        }

        public static Feature intFeature(String name, Integer value) {
            notNull(value);
            return new Feature(name, Type.INTEGER, ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
        }

        public static Feature longFeature(String name, Long value) {
            notNull(value);
            return new Feature(name, Type.LONG, ByteBuffer.allocate(Long.BYTES).putLong(value).array());
        }

        public static Feature floatFeature(String name, Float value) {
            notNull(value);
            return new Feature(name, Type.FLOAT, ByteBuffer.allocate(Float.BYTES).putFloat(value).array());
        }

        public static Feature doubleFeature(String name, Double value) {
            notNull(value);
            return new Feature(name, Type.DOUBLE, ByteBuffer.allocate(Double.BYTES).putDouble(value).array());
        }

        public static Feature bigIntegerFeature(String name, BigInteger value) {
            notNull(value);
            return new Feature(name, Type.BIGINTEGER, value.toByteArray());
        }

        public static Feature bigDecimalFeature(String name, BigDecimal value) {
            notNull(value);
            byte[] b = value.unscaledValue().toByteArray();
            return new Feature(name, Type.BIGDECIMAL, ByteBuffer.allocate(Integer.BYTES + b.length)
                    .put(b)
                    .putInt(value.scale())
                    .array());
        }

        public static Feature dateFeature(String name, Date value) {
            notNull(value);
            return new Feature(name, Type.DATE, ByteBuffer.allocate(Long.BYTES).putLong(value.getTime()).array());
        }

        public static Feature from(byte[] serialized) {
            if (serialized.length < Integer.BYTES * 3) {
                throw new IllegalArgumentException("Cannot load feature from a byte array that has "
                        + serialized.length + " bytes which is < " + (3 * Integer.BYTES));
            }
            var bb = ByteBuffer.wrap(serialized);
            var typeSerialized = bb.getInt();
            final Type type = typeFrom(typeSerialized);
            final var nameLength = bb.getInt();
            final var valueLength = bb.getInt();
            if (type.fixedSize != VARIABLE_LENGTH && type.fixedSize != valueLength) {
                throw new IllegalArgumentException("Cannot load a " + type.toString() + " type feature from " +
                        valueLength + " bytes. It needs exactly " + type.fixedSize + " bytes.");
            }
            final var expectedLength = Integer.BYTES * 3 + valueLength + nameLength;
            if (serialized.length != expectedLength) {
                throw new IllegalArgumentException("Cannot load feature from a byte array that has "
                        + serialized.length + " bytes which is != " + expectedLength);
            }
            final var nameBuffer = new byte[nameLength];
            bb.get(nameBuffer);
            final var value = new byte[valueLength];
            if (valueLength > 0) {
                bb.get(value);
            }
            final var name = new String(nameBuffer, StandardCharsets.UTF_8);
            return new Feature(name, type, value);
        }

        private static Type typeFrom(int typeSerialized) {
            for (final var type : Type.values()) {
                if (type.serialized == typeSerialized) {
                    return type;
                }
            }
            throw new IllegalArgumentException("The deserialized form has a type value " + typeSerialized + " which is not valid.");
        }
    }
}

