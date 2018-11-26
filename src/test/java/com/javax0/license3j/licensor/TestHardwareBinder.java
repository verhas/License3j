package com.javax0.license3j.licensor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

public class TestHardwareBinder {

    private static final boolean falseTrue[] = new boolean[]{false, true};

    @Test
    @DisplayName("calling hardwarebinder main() does not throw up")
    public void testMain() throws UnsupportedEncodingException,
            SocketException, UnknownHostException {
        HardwareBinder.main(null);
    }

    @Test
    @DisplayName("hardware binder should accept the machine UUID it just calculated on the test machine")
    public void machineHasUuid() throws UnsupportedEncodingException,
            SocketException, UnknownHostException {
        for (final boolean ignoreNetwork : falseTrue) {
            for (final boolean ignoreArchitecture : falseTrue) {
                for (final boolean ignoreHostName : falseTrue) {
                    final HardwareBinder hb = new HardwareBinder();
                    if (ignoreNetwork)
                        hb.ignoreNetwork();
                    if (ignoreArchitecture)
                        hb.ignoreArchitecture();
                    if (ignoreHostName)
                        hb.ignoreHostName();
                    final UUID uuid = hb.getMachineId();
                    Assertions.assertTrue(hb.assertUUID(uuid));
                }
            }
        }
    }

    @Test
    @DisplayName("hardware binder should accept the machine UUID string format it just calculated on the test machine")
    public void machineHasUuidString() throws UnsupportedEncodingException,
            SocketException, UnknownHostException {
        for (final boolean ignoreNetwork : falseTrue) {
            for (final boolean ignoreArchitecture : falseTrue) {
                for (final boolean ignoreHostName : falseTrue) {
                    final HardwareBinder hb = new HardwareBinder();
                    if (ignoreNetwork)
                        hb.ignoreNetwork();
                    if (ignoreArchitecture)
                        hb.ignoreArchitecture();
                    if (ignoreHostName)
                        hb.ignoreHostName();
                    final String uuidS = hb.getMachineIdString();
                    Assertions.assertTrue(hb.assertUUID(uuidS));
                }
            }
        }
    }

    @Test
    @DisplayName("if the UUID string is too long UUID assertion returns false")
    public void tooLongUuidStringAssertsFalse()
            throws UnsupportedEncodingException, SocketException,
            UnknownHostException {
        for (final boolean ignoreNetwork : falseTrue) {
            for (final boolean ignoreArchitecture : falseTrue) {
                for (final boolean ignoreHostName : falseTrue) {
                    final HardwareBinder hb = new HardwareBinder();
                    if (ignoreNetwork)
                        hb.ignoreNetwork();
                    if (ignoreArchitecture)
                        hb.ignoreArchitecture();
                    if (ignoreHostName)
                        hb.ignoreHostName();
                    final String uuid = hb.getMachineIdString();
                    final String buuid = uuid + "*";
                    Assertions.assertFalse(hb.assertUUID(buuid));
                }
            }
        }
    }

    private String alterLastHexaChar(final String s) {
        String lastChar = s.substring(s.length() - 1);
        if (lastChar.equals("f")) {
            lastChar = "e";
        } else {
            lastChar = "f";
        }
        return s.substring(0, s.length() - 1) + lastChar;

    }

    @Test
    @DisplayName("if the UUID string is properly formatted but contains a wrong value then UUID assertion returns false")
    public void wrongUuidStringAssertsFalse()
            throws UnsupportedEncodingException, SocketException,
            UnknownHostException {
        for (final boolean ignoreNetwork : falseTrue) {
            for (final boolean ignoreArchitecture : falseTrue) {
                for (final boolean ignoreHostName : falseTrue) {
                    final HardwareBinder hb = new HardwareBinder();
                    if (ignoreNetwork)
                        hb.ignoreNetwork();
                    if (ignoreArchitecture)
                        hb.ignoreArchitecture();
                    if (ignoreHostName)
                        hb.ignoreHostName();
                    final String uuid = alterLastHexaChar(hb.getMachineIdString());
                    Assertions.assertFalse(hb.assertUUID(uuid));
                }
            }
        }
    }
}
