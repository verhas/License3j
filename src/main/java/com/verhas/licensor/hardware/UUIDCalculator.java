package com.verhas.licensor.hardware;

import org.bouncycastle.crypto.digests.MD5Digest;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

public class UUIDCalculator {
    private final HashCalculator calculator;

    public UUIDCalculator(InterfaceSelector selector) {
        this.calculator = new HashCalculator(selector);
    }

    public UUID getMachineId(boolean useNetwork, boolean useHostName, boolean useArchitecture) throws UnsupportedEncodingException,
            SocketException, UnknownHostException {
        final MD5Digest md5 = new MD5Digest();
        md5.reset();
        if (useNetwork) {
            calculator.updateWithNetworkData(md5);
        }
        if (useHostName) {
            calculator.updateWithHostName(md5);
        }
        if (useArchitecture) {
            calculator.updateWithArchitecture(md5);
        }
        final byte[] digest = new byte[16];
        md5.doFinal(digest, 0);
        return UUID.nameUUIDFromBytes(digest);
    }

    public String getMachineIdString(boolean useNetwork, boolean useHostName, boolean useArchitecture) throws UnsupportedEncodingException,
            SocketException, UnknownHostException {
        final UUID uuid = getMachineId(useNetwork, useHostName, useArchitecture);
        if (uuid != null) {
            return uuid.toString();
        } else {
            return null;
        }
    }

    public boolean assertUUID(final UUID uuid, boolean useNetwork, boolean useHostName, boolean useArchitecture)
            throws UnsupportedEncodingException, SocketException,
            UnknownHostException {
        final UUID machineUUID = getMachineId(useNetwork, useHostName, useArchitecture);
        return machineUUID != null && machineUUID.equals(uuid);
    }

    public boolean assertUUID(final String uuid, boolean useNetwork, boolean useHostName, boolean useArchitecture) {
        try {
            return assertUUID(java.util.UUID.fromString(uuid), useNetwork, useHostName, useArchitecture);
        } catch (Exception e) {
            return false;
        }
    }
}
