package com.verhas.licensor.hardware;

import org.bouncycastle.crypto.digests.MD5Digest;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;

class HashCalculator {
    private final InterfaceSelector selector;

    HashCalculator(InterfaceSelector selector) {
        this.selector = selector;
    }

    private void updateWithNetworkData(final MD5Digest md5,
                                       final List<NetworkInterfaceData> networkInterfaces)
            throws UnsupportedEncodingException {
        for (final NetworkInterfaceData ni : networkInterfaces) {
            md5.update(ni.name.getBytes("utf-8"), 0,
                    ni.name.getBytes("utf-8").length);
            if (ni.hwAddress != null) {
                md5.update(ni.hwAddress, 0, ni.hwAddress.length);
            }
        }
    }

    void updateWithNetworkData(final MD5Digest md5)
            throws UnsupportedEncodingException, SocketException {
        final List<NetworkInterfaceData> networkInterfaces = NetworkInterfaceData.gatherUsing(selector);
        networkInterfaces.sort(Comparator.comparing(a -> a.name));
        updateWithNetworkData(md5, networkInterfaces);
    }

    void updateWithHostName(final MD5Digest md5)
            throws UnknownHostException, UnsupportedEncodingException {
        final String hostName = java.net.InetAddress.getLocalHost()
                .getHostName();
        md5.update(hostName.getBytes("utf-8"), 0,
                hostName.getBytes("utf-8").length);
    }

    void updateWithArchitecture(final MD5Digest md5)
            throws UnsupportedEncodingException {
        final String architectureString = System.getProperty("os.arch");
        md5.update(architectureString.getBytes("utf-8"), 0,
                architectureString.getBytes("utf-8").length);
    }
}
