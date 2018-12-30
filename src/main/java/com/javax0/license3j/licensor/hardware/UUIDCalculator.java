package com.javax0.license3j.licensor.hardware;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDCalculator {
    private final HashCalculator calculator;

    public UUIDCalculator(Network.Interface.Selector selector) {
        this.calculator = new HashCalculator(selector);
    }

    public UUID getMachineId(boolean useNetwork, boolean useHostName, boolean useArchitecture) throws
        SocketException, UnknownHostException, NoSuchAlgorithmException {
        final var md5 = MessageDigest.getInstance("MD5");
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
        final byte[] digest = md5.digest();
        return UUID.nameUUIDFromBytes(digest);
    }

    public String getMachineIdString(boolean useNetwork, boolean useHostName, boolean useArchitecture) throws
        SocketException, UnknownHostException, NoSuchAlgorithmException {
        final UUID uuid = getMachineId(useNetwork, useHostName, useArchitecture);
        if (uuid != null) {
            return uuid.toString();
        } else {
            return null;
        }
    }

    public boolean assertUUID(final UUID uuid, boolean useNetwork, boolean useHostName, boolean useArchitecture)
        throws SocketException, UnknownHostException, NoSuchAlgorithmException {
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
