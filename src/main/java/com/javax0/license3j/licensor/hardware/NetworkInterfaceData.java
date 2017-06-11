package com.javax0.license3j.licensor.hardware;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A data class holding the network interface data.
 *
 * @author Peter Verhas
 */
class NetworkInterfaceData {
    public final String name;
    byte[] hwAddress;

    private NetworkInterfaceData(final NetworkInterface networkInterface) {
        name = networkInterface.getName();
        try {
            hwAddress = networkInterface.getHardwareAddress();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    static List<NetworkInterfaceData> gatherUsing(InterfaceSelector selector)
            throws SocketException {
        return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .filter(selector::usable)
                .map(NetworkInterfaceData::new).collect(Collectors.toList());
    }
}
