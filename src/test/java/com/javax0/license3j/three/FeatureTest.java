package com.javax0.license3j.three;

import com.javax0.license3j.three.Feature.Create;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

public class FeatureTest {

    @Test
    @DisplayName("zero length binary feature, serialize and restore")
    public void testZeroLengthBinarySerializationAndRestore() {
        var sut = Create.binaryFeature("feature name", new byte[0]);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(0, res.getBinary().length);
    }

    @Test
    @DisplayName("one length binary feature, serialize and restore")
    public void testOneLengthBinarySerializationAndRestore() {
        var sut = Create.binaryFeature("feature name", new byte[]{(byte) 0xFE});
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(1, res.getBinary().length);
        Assertions.assertEquals((byte) 0xFE, res.getBinary()[0]);
    }

    @Test
    @DisplayName("zero length string feature, serialize and restore")
    public void testZeroLengthStringSerializationAndRestore() {
        var sut = Create.stringFeature("feature name", "");
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(0, res.getString().length());
    }

    @Test
    @DisplayName("string feature, serialize and restore")
    public void testString() {
        var sut = Create.stringFeature("feature name", "Hello, World!");
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals("Hello, World!", res.getString());
    }

    @Test
    @DisplayName("byte feature, serialize and restore")
    public void testByte() {
        var sut = Create.byteFeature("feature name", (byte) 0xFE);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals((byte) 0xFE, res.getByte());
    }

    @Test
    @DisplayName("short feature, serialize and restore")
    public void testShort() {
        var sut = Create.shortFeature("feature name", (short) 0xFEFE);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals((short) 0xFEFE, res.getShort());
    }

    @Test
    @DisplayName("int feature, serialize and restore")
    public void testInt() {
        var sut = Create.intFeature("feature name", 0xFEFEFEFE);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(0xFEFEFEFE, res.getInt());
    }

    @Test
    @DisplayName("long feature, serialize and restore")
    public void testLong() {
        var sut = Create.longFeature("feature name", 0xFEFEFEFEFEFEFEFEL);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(0xFEFEFEFEFEFEFEFEL, res.getLong());
    }

    @Test
    @DisplayName("float feature, serialize and restore")
    public void testFloat() {
        var sut = Create.floatFeature("feature name", (float) 3.1415926);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals((float) 3.1415926, res.getFloat());
    }

    @Test
    @DisplayName("double feature, serialize and restore")
    public void testDouble() {
        var sut = Create.doubleFeature("feature name", 3.1415926);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(3.1415926, res.getDouble());
    }

    @Test
    @DisplayName("BigInteger feature, serialize and restore")
    public void testBigInteger() {
        var bi = new BigInteger("1377277372717772737372717727371723777273177172737727371984940591");
        var sut = Create.bigIntegerFeature("feature name", bi);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(bi, res.getBigInteger());
    }

    @Test
    @DisplayName("BigDecimal feature, serialize and restore")
    public void testBigDecimal() {
        var bd = new BigDecimal("3.141592653589793238462643383279502884197169399375105820974"
                + "944592307816406286208998628034825342117067982148086513282306647093844609550582"
                + "231725359408128481117450284102701938521105559644622948954930381964428810975665"
                + "93344612847564823378678316527120190914564856692346034861045432664");
        var sut = Create.bigDecimalFeature("feature name", bd);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(bd, res.getBigDecimal());
    }

    @Test
    @DisplayName("Date feature, serialize and restore")
    public void testDate() {
        var now = new Date();
        var sut = Create.dateFeature("feature name", now);
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(now, res.getDate());
    }

    @Test
    @DisplayName("'from()' throws exception when the type is wrong value serialized")
    public void testWrongTypeDeserialization() {
        var sut = Create.binaryFeature("name", new byte[0]);
        var b = sut.serialized();
        b[3] = 0;
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Create.from(b));
    }

    @Test
    @DisplayName("'from()' throws exception when array is too short")
    public void testShortDeserialization() {
        var b = new byte[4];
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Create.from(b));
    }

    @Test
    @DisplayName("'from()' throws exception when the array is truncated")
    public void testTruncatedDeserialization() {
        var sut = Create.binaryFeature("name", new byte[]{(byte)0xFE,(byte)0xFE});
        var b = sut.serialized();
        var c = Arrays.copyOf(b,b.length-1);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Create.from(c));
    }

    @Test
    @DisplayName("'from()' throws exception when the array is padded")
    public void testPaddedDeserialization() {
        var sut = Create.binaryFeature("name", new byte[]{(byte)0xFE,(byte)0xFE});
        var b = sut.serialized();
        var c = Arrays.copyOf(b,b.length+1);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Create.from(c));
    }

    @Test
    @DisplayName("Creating from null throws exception")
    public void creationFromNull() {
        for (var method : Create.class.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && method.getName().endsWith("Feature")) {
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> {
                            try {
                                method.invoke(null, "", null);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        });
            }
        }
    }
}
