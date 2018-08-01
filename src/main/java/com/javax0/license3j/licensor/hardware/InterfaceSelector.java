package com.javax0.license3j.licensor.hardware;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class InterfaceSelector {

    private final Set<String> allowedInterfaceNames = new HashSet<>();
    private final Set<String> deniedInterfaceNames = new HashSet<>();

    /**
     * @param string   to match
     * @param regexSet regular expressions provided as set of strings
     * @return true if the {@code string} matches any of the regular expressions
     */
    private static boolean matchesAny(final String string, Set<String> regexSet) {
        return regexSet.stream().anyMatch(string::matches);
    }

    /**
     * Checks the sets of regular expressions against the display name of the
     * network interface. If there is a set of denied names then if any of the
     * regular expressions matches the name of the interface then the interface
     * is denied. If there is no denied set then the processing is not affected
     * by the non existence. In other word not specifying any denied interface
     * name means that no interface is denied explicitly.
     * <p>
     * If there is a set of permitted names then if any of the regular
     * expressions matches the name of the interface then the interface is
     * permitted. If there is no set then the interface is permitted. In other
     * words it is not possible to deny all interfaces specifying an empty set.
     * Although this would mathematically logical, but there is no valuable use
     * case that would require this feature.
     * <p>
     * Note that the name, which is checked is not the basic name (e.g.
     * <tt>eth0</tt>) but the display name, which is more human readable.
     *
     * @param netIf the netrowk interface
     * @return {@code true} if the interface has to be taken into the
     * calculation of the license and {@code false} (ignore the
     * interface) otherwise.
     */
    private boolean matchesRegexLists(final NetworkInterface netIf) {
        final String name = netIf.getDisplayName();

        return !matchesAny(name, deniedInterfaceNames)
            &&
            (allowedInterfaceNames.isEmpty() ||
                matchesAny(name, allowedInterfaceNames));
    }

    public void interfaceAllowed(String regex) {
        allowedInterfaceNames.add(regex);
    }

    public void interfaceDenied(String regex) {
        deniedInterfaceNames.add(regex);
    }

    /**
     * @param netIf the network interface
     * @return {@code true} if the actual network interface has to be used for
     * the calculation of the hardware identification id.
     */
    boolean usable(final NetworkInterface netIf) {
        try {
            return !isSpecial(netIf)
                && matchesRegexLists(netIf);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isSpecial(NetworkInterface netIf) throws SocketException {
        return netIf.isLoopback() || netIf.isVirtual() || netIf.isPointToPoint();
    }
}
