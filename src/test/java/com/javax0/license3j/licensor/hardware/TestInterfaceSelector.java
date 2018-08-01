package com.javax0.license3j.licensor.hardware;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.NetworkInterface;
import java.net.SocketException;

public class TestInterfaceSelector {


    private static InterfaceSelector newSut() {

        final var sut = new InterfaceSelector() {
            boolean isSpecial(NetworkInterface netIf) throws SocketException {
                return false;
            }
        };
        return sut;
    }

    private NetworkInterface mockInterface(final String name)
        throws NoSuchFieldException,
        IllegalAccessException,
        InvocationTargetException,
        InstantiationException {
        Constructor constructor = NetworkInterface.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        NetworkInterface ni = (NetworkInterface) constructor.newInstance();
        Field field = NetworkInterface.class.getDeclaredField("displayName");
        field.setAccessible(true);
        field.set(ni,name);
        return ni;
    }

    @Test
    public void nonConfiguredSelectorAllowsAllInterfaces() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var sut = newSut();
        var ni = mockInterface("just-any-name");
        Assert.assertTrue(sut.usable(ni));
    }

    @Test
    public void configuredAllowedIsAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var sut = newSut();
        sut.interfaceAllowed("allowed");
        var ni = mockInterface("allowed");
        Assert.assertTrue(sut.usable(ni));
    }

    @Test
    public void configuredAllowedNotInConfigIsAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var sut = newSut();
        sut.interfaceAllowed("allowed");
        var ni = mockInterface("not allowed");
        Assert.assertFalse(sut.usable(ni));
    }

    @Test
    public void configuredAllowedDeniedIsDenied() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var sut = newSut();
        sut.interfaceAllowed("allowed");
        sut.interfaceDenied("denied");
        var ni = mockInterface("denied");
        Assert.assertFalse(sut.usable(ni));
    }

    @Test
    public void configuredAllowedDeniedIsAllowed() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        var sut = newSut();
        sut.interfaceAllowed("allowed");
        sut.interfaceDenied("denied");
        var ni = mockInterface("allowed");
        Assert.assertTrue(sut.usable(ni));
    }
}
