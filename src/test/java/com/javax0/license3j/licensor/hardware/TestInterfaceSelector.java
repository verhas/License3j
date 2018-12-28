package com.javax0.license3j.licensor.hardware;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.NetworkInterface;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInterfaceSelector {
    private static final boolean USABLE = true;
    private static final boolean EXCLUDED = false;

    private static Network.Interface.Selector newSut() {

        return new Network.Interface.Selector() {
            boolean isSpecial(NetworkInterface netIf) {
                return false;
            }
        };
    }

    private static IfTest test(final String ifName) throws NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return new IfTest(ifName);
    }

    private static NetworkInterface mockInterface(final String name)
            throws NoSuchFieldException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        Constructor constructor = NetworkInterface.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        NetworkInterface ni = (NetworkInterface) constructor.newInstance();
        Field field = NetworkInterface.class.getDeclaredField("displayName");
        field.setAccessible(true);
        field.set(ni, name);
        return ni;
    }

    @Test
    @DisplayName("If there is no regular expression defined as allowed nor as denied then everything is allowed")
    public void testJustAnyName() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("just-any-name").isUsable();
    }

    @Test
    @DisplayName("If there is a regular expression allowing an interface then an interface matching the regex will be allowed")
    public void explicitlyAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("allowed").allowed("allowed").isUsable();
    }

    @Test
    @DisplayName("If there is a regular expression allowing an interface then an interface NOT matching the regex will be denied")
    public void explicitlyNotAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("not allowed").allowed("allowed").isDenied();
    }

    @Test
    @DisplayName("If there is a regular expression denying an interface then an interface matching the regex will be denied")
    public void explicitlyDenied() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("denied").allowed("allowed").denied("denied").isDenied();
    }

    @Test
    @DisplayName("If there is a regular expression denying an interface then an interface matching the regex will be denied EVEN if it matches an regex allowing it")
    public void explicitlyDeniedEvenIfAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("denied").allowed("denied").denied("denied").isDenied();
    }

    @Test
    @DisplayName("If there is a regular expression allowing it and the denying regex does not match then it is allowed")
    public void explicitlyAllowedNotDenied() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("allowed").allowed("allowed").denied("denied").isUsable();
    }

    @Test
    @DisplayName("If there is a regular expression allowing it and the denying regexes do not match then it is allowed")
    public void explicitlyAllowedNotDeniedByAny() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        test("allowed").allowed("allowed").denied("denied","denied2").isUsable();
    }

    private static class IfTest {
        NetworkInterface ni;
        Network.Interface.Selector sut;

        IfTest(String name) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
            ni = mockInterface(name);
            sut = newSut();
        }

        IfTest allowed(String... alloweds) {
            for (var allowed : alloweds) {
                sut.interfaceAllowed(allowed);
            }
            return this;
        }

        IfTest denied(String... denieds) {
            for (var denied : denieds) {
                sut.interfaceDenied(denied);
            }
            return this;
        }

        void isUsable() {
            assertTrue(sut.usable(ni));
        }

        void isDenied() {
            assertFalse(sut.usable(ni));
        }

    }
}
