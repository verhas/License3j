package com.javax0.license3j.three;

import com.javax0.license3j.three.Feature.Create;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

public class FeatureTest {

    private static Method findMethod(final String methodName, Method[] methods, Class klass) {
        for (final var method : methods) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                return method;
            }
        }
        Assertions.fail("Method " + methodName + " was not found in class " + klass.getSimpleName());
        return null;
    }

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
    @DisplayName("binary feature, serialize and restore")
    public void testBinary() {
        var sut = Create.binaryFeature("feature name", new byte[]{(byte) 0xFE, (byte) 0x53, (byte) 0xFF});
        byte[] b = sut.serialized();
        var res = Create.from(b);
        Assertions.assertEquals(3, res.getBinary().length);
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
        var sut = Create.binaryFeature("name", new byte[]{(byte) 0xFE, (byte) 0xFE});
        var b = sut.serialized();
        var c = Arrays.copyOf(b, b.length - 1);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Create.from(c));
    }

    @Test
    @DisplayName("'from()' throws exception when the array is padded")
    public void testPaddedDeserialization() {
        var sut = Create.binaryFeature("name", new byte[]{(byte) 0xFE, (byte) 0xFE});
        var b = sut.serialized();
        var c = Arrays.copyOf(b, b.length + 1);
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

    @Test
    @DisplayName("Check that all types have a factory method")
    public void testFactories() throws NoSuchFieldException, IllegalAccessException {
        var methods = Feature.Create.class.getDeclaredMethods();
        for (final var type : getTypeValues()) {
            final var methodName = type.toString() + "Feature";
            var method = findMethod(methodName, methods, Feature.Create.class);
            var modifiers = method.getModifiers();
            Assertions.assertTrue(Modifier.isStatic(modifiers), "Method "
                    + methodName + " is not static.");
            Assertions.assertTrue(Modifier.isPublic(modifiers), "Method "
                    + methodName + " is not public.");
            var argtypes = method.getParameterTypes();
            Assertions.assertEquals(2, argtypes.length, "Factory method "
                    + methodName + " should have exactly two arguments.");
            Assertions.assertEquals(String.class, argtypes[0], "Factory method "
                    + methodName + " first argument has to be String.");
            if (argtypes[1].isArray()) {
                var constField = Feature.class.getDeclaredField("VARIABLE_LENGTH");
                constField.setAccessible(true);
                final int VARIABLE_LENGTH = (int) constField.get(null);
                Assertions.assertEquals(VARIABLE_LENGTH, getFixedSize(type), "array accepting factory "
                        + methodName + " size has to be VARIABLE_LENGTH");
            }
        }
    }

    private Object getFixedSize(Object type) {
        try {
            final Class<?> typeClass = getTypeClass();
            final var fixedSize = typeClass.getDeclaredField("fixedSize");
            fixedSize.setAccessible(true);
            return fixedSize.get(type);
        } catch (Exception e) {
            throw new AssertionError("Cannot get the types reflectively");
        }
    }

    private Object[] getTypeValues() {
        try {
            final Class<?> typeClass = getTypeClass();
            final var values = typeClass.getDeclaredMethod("values");
            return (Object[]) values.invoke(null);
        } catch (Exception e) {
            throw new AssertionError("Cannot get the types reflectively");
        }
    }

    private Class<?> getTypeClass() throws ClassNotFoundException {
        return Class.forName(Feature.class.getName() + "$Type");
    }

    @Test
    @DisplayName("Check that all tests there are that serialize and deserialize some type")
    public void testSerializeDeserializeTestCompleteness() {
        var methods = this.getClass().getDeclaredMethods();
        for (final var type : getTypeValues()) {
            final var methodName = "test" + type.toString();
            var method = findMethod(methodName, methods, this.getClass());
            var modifiers = method.getModifiers();
            Assertions.assertTrue(!Modifier.isStatic(modifiers), "Method " + methodName + " is static.");
            Assertions.assertTrue(Modifier.isPublic(modifiers), "Method " + methodName + " is not public.");
            var argtypes = method.getParameterTypes();
            Assertions.assertEquals(0, argtypes.length, "Test method " + methodName + " should not have parameters.");
        }
    }

    @Test
    @DisplayName("When asking for the wrong type method throws exception")
    public void testGetWrongType() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Method[] methods = Feature.class.getDeclaredMethods();
        final Constructor<?> constructor = getFeatureConstructor();
        for (final var type : getTypeValues()) {
            final Object sut = createFeatureSut(constructor, type);
            for (final var wrongType : getTypeValues()) {
                if (type != wrongType) {
                    final var methodName = "get" + wrongType.toString();
                    final var getMethod = findMethod(methodName, methods, Feature.class);
                    final var modifiers = getMethod.getModifiers();
                    Assertions.assertTrue(Modifier.isPublic(modifiers), "Method " + getMethod.getName() + " is supposed to be public.");
                    Assertions.assertThrows(IllegalArgumentException.class, () -> {
                        try {
                            getMethod.invoke(sut);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }, "No exception was thrown when calling " + getMethod.getName() + " for type " + wrongType);
                }
            }
        }
    }

    private Object createFeatureSut(Constructor<?> constructor, Object type)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return constructor.newInstance("name", type, new byte[]{0x00});
    }


    @Test
    @DisplayName("Testing type with isType() method works")
    public void testTestType() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Method[] methods = Feature.class.getDeclaredMethods();
        final Constructor<?> constructor = getFeatureConstructor();
        for (final var type : getTypeValues()) {
            final Object sut = createFeatureSut(constructor, type);
            for (final var testType : getTypeValues()) {
                final var methodName = "is" + testType.toString();
                final var getMethod = findMethod(methodName, methods, Feature.class);
                final var modifiers = getMethod.getModifiers();
                Assertions.assertTrue(Modifier.isPublic(modifiers),
                        "Method " + getMethod.getName() + " is supposed to be public.");
                Assertions.assertTrue((boolean) getMethod.invoke(sut) == (testType == type),
                        "method " + getMethod.getName() +
                                " does not test properly a Feature that is of type " + testType);
            }
        }
    }

    private Constructor<?> getFeatureConstructor() {
        final var constructors = Feature.class.getDeclaredConstructors();
        final var constructor = Arrays.stream(constructors)
                .filter(c -> c.getParameterCount() == 3)
                .findAny()
                .orElseThrow(() -> new AssertionFailedError("There is no appropriate constructor in the class Feature"));
        final var constructorModifiers = constructor.getModifiers();
        Assertions.assertTrue(Modifier.isPrivate(constructorModifiers), "The constructor of Feature has to be private.");
        constructor.setAccessible(true);
        return constructor;
    }
}

