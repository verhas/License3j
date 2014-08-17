package com.verhas.licensor;

import static com.verhas.utils.Sugar.matchesAny;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bouncycastle.crypto.digests.MD5Digest;

/**
 * The hardware binder binds a license to a certain hardware. The use of this
 * feature is optional. Calling methods from this class the license manager can
 * check that the license is deployed on the machine it was destined and may
 * decide NOT to work on other machines than it was destined to.
 * <p>
 * It is recommended that such a checking is used only with warning purposes and
 * not treated as a strict license violation. It may happen that the ethernet
 * card of a server is replaced due to some failure and there is no time to
 * request a new license.
 * <p>
 * Therefore it is a recommended practice to note the disalignment of the
 * license and send it to the log, but do not deter the operation of the
 * software.
 * 
 * @author Peter Verhas
 */
public class HardwareBinder {

	/**
	 * A data class holding the network interface data.
	 * 
	 * @author Peter Verhas
	 * 
	 */
	private class NetworkInterfaceData {
		public NetworkInterfaceData(final NetworkInterface networkInterface)
				throws SocketException {
			name = networkInterface.getName();
			hwAddress = networkInterface.getHardwareAddress();
		}

		String name;
		byte[] hwAddress;
	}

	private boolean useHostName = true;

	/**
	 * When calculating the machine UUID the host name is also taken into
	 * account by default. If you want the method to ignore the machine name
	 * then call this method before calling any UUID calculation method.
	 */
	public void ignoreHostName() {
		useHostName = false;
	}

	private boolean useNetwork = true;

	/**
	 * When calculating the uuid of a machine the network interfaces are
	 * enumerated and their parameters are taken into account. The names and the
	 * hardware addresses are used.
	 * <p>
	 * If you want to ignore the network when generating the uuid then call this
	 * method before any uuid calculating methods.
	 */
	public void ignoreNetwork() {
		useNetwork = false;
	}

	private boolean useArchitecture = true;

	/**
	 * The UUID generation uses the architecture string as returned by
	 * {@code System.getProperty("os.arch")}. In some rare cases you want to
	 * have a UUID that is independent of the architecture.
	 */
	public void ignoreArchitecture() {
		useArchitecture = false;
	}

	private int numberOfInterfaces() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = java.net.NetworkInterface
				.getNetworkInterfaces();
		int interfaceCounter = 0;
		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces
					.nextElement();
			if (weShouldUseForTheCalculationThis(networkInterface)) {
				interfaceCounter++;
			}
		}
		return interfaceCounter;
	}

	private final Set<String> allowedInterfaceNames = new HashSet<String>();
	private final Set<String> deniedInterfaceNames = new HashSet<String>();

	/**
	 * Add a regular expression to the set of the regular expressions that are
	 * checked against the display name of the network interface cards. If any
	 * of the regular expressions are matched against the display name then the
	 * interface is allowed taken into account during the calculation of the
	 * machine id.
	 * <p>
	 * Note that there is also a denied set of regular expressions. A network
	 * interface card is used during the calculation of the machine uuid if any
	 * of the allowing regular expressions match and none of the denying regular
	 * expressions match.
	 * <p>
	 * Note that if there is no any allowing regular expressions, then this is
	 * treated that all the interface cards are allowed unless explicitly denied
	 * by any of the denying regular expressions. This way the functionality of
	 * the hardware binder class is compatible with previous versions. If you
	 * define nor allowed set, neither denied set then the interface cards are
	 * treated the same as with the old version.
	 * <p>
	 * This functionality is needed only when you have problem with some virtual
	 * network interface cards that are erroneously reported by the Java run
	 * time system as physical cards. This is a well known bug that is low
	 * priority in the Java realm and there is no general workaround. If you
	 * face that problem, then try programmatically exclude from the calculation
	 * the network cards that cause you problem.
	 * 
	 * @param regex
	 */
	public void interfaceAllowed(String regex) {
		allowedInterfaceNames.add(regex);
	}

	/**
	 * Add a regular expression to the set of the regular expressions that are
	 * checked against the display name of the network interface cards. If any
	 * of the regular expressions are matched against the display name then the
	 * interface is denied taken into account during the calculation of the
	 * machine id.
	 * <p>
	 * See also the documentation of the method
	 * {@link #interfaceAllowed(String)}.
	 * 
	 * @param regex
	 */
	public void interfaceDenied(String regex) {
		deniedInterfaceNames.add(regex);
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
	 * @param networkInterface
	 * @return {@code true} if the interface has to be taken into the
	 *         calculation of the license and {@code false} (ignore the
	 *         interface) otherwise.
	 */
	private boolean matchesRegexLists(final NetworkInterface networkInterface) {
		String interfaceName = networkInterface.getDisplayName();

		return !matchesAny(interfaceName, deniedInterfaceNames)
				&& (allowedInterfaceNames.size() == 0 || matchesAny(
						interfaceName, allowedInterfaceNames));
	}

	/**
	 * 
	 * @param networkInterface
	 * @return {@code true} if the actual network interface has to be used for
	 *         the calculation of the hardware identification id.
	 * @throws SocketException
	 */
	private boolean weShouldUseForTheCalculationThis(
			final NetworkInterface networkInterface) throws SocketException {
		return !networkInterface.isLoopback() && !networkInterface.isVirtual()
				&& !networkInterface.isPointToPoint()
				&& matchesRegexLists(networkInterface);
	}

	private NetworkInterfaceData[] networkInterfaceData()
			throws SocketException {
		final NetworkInterfaceData[] networkInterfaceArray = new NetworkInterfaceData[numberOfInterfaces()];
		int index = 0;
		// collect the interface properties
		final Enumeration<NetworkInterface> networkInterfaces = java.net.NetworkInterface
				.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces
					.nextElement();
			if (weShouldUseForTheCalculationThis(networkInterface)) {
				networkInterfaceArray[index] = new NetworkInterfaceData(
						networkInterface);
				index++;
			}
		}
		return networkInterfaceArray;
	}

	/**
	 * SORT the network interfaces. We do not rely on that non-guaranteed feature
	 * that getNetworkInterfaces() returns the interfaces the same order always
	 */
	private void sortNetworkInterfaces(final NetworkInterfaceData[] networkInterfaceData) {
		Arrays.sort(networkInterfaceData, new Comparator<NetworkInterfaceData>() {
                        @Override
			public int compare(final NetworkInterfaceData a,
					final NetworkInterfaceData b) {
				return a.name.compareTo(b.name);
			}
		});
	}

	private void updateWithNetworkData(final MD5Digest md5,
			final NetworkInterfaceData[] networkInterfaces)
			throws UnsupportedEncodingException {
		for (final NetworkInterfaceData ni : networkInterfaces) {
			md5.update(ni.name.getBytes("utf-8"), 0,
					ni.name.getBytes("utf-8").length);
			if (ni.hwAddress != null) {
				md5.update(ni.hwAddress, 0, ni.hwAddress.length);
			}
		}
	}

	private void updateWithNetworkData(final MD5Digest md5)
			throws UnsupportedEncodingException, SocketException {
		final NetworkInterfaceData[] networkInterfaces = networkInterfaceData();
		sortNetworkInterfaces(networkInterfaces);
		updateWithNetworkData(md5, networkInterfaces);
	}

	private void updateWithHostName(final MD5Digest md5)
			throws UnknownHostException, UnsupportedEncodingException {
		final String hostName = java.net.InetAddress.getLocalHost()
				.getHostName();
		md5.update(hostName.getBytes("utf-8"), 0,
				hostName.getBytes("utf-8").length);
	}

	private void updateWithArchitecture(final MD5Digest md5)
			throws UnsupportedEncodingException {
		final String architectureString = System.getProperty("os.arch");
		md5.update(architectureString.getBytes("utf-8"), 0,
				architectureString.getBytes("utf-8").length);
	}

	/**
	 * Calculate the UUID for the machine this code is running on. To do this
	 * the method lists all network interfaces that are real 'server' interfaces
	 * (ignoring loop-back, virtual, and point-to-point interfaces). The method
	 * takes each interface name (as a string) and hardware address into a MD5
	 * digest one after the other and finally converts the resulting 128bit
	 * digest into a UUID.
	 * <p>
	 * The method also feeds the local machine name into the digest.
	 * <p>
	 * This method relies on Java 6 methods, but also works with Java 5. However
	 * the result will not be the same on Java 5 as on Java 6.
	 * 
	 * @return the UUID of the machine or null if the uuid can not be
	 *         calculated.
	 * @throws SocketException
	 * @throws UnsupportedEncodingException
	 * @throws UnknownHostException
	 */
	public UUID getMachineId() throws UnsupportedEncodingException,
			SocketException, UnknownHostException {
		final MD5Digest md5 = new MD5Digest();
		md5.reset();
		if (useNetwork) {
			updateWithNetworkData(md5);
		}
		if (useHostName) {
			updateWithHostName(md5);
		}
		if (useArchitecture) {
			updateWithArchitecture(md5);
		}
		final byte[] digest = new byte[16];
		md5.doFinal(digest, 0);
		return UUID.nameUUIDFromBytes(digest);
	}

	/**
	 * Get the machine id as an UUID string.
	 * 
	 * @return the UUID as a string
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws UnsupportedEncodingException
	 */
	public String getMachineIdString() throws UnsupportedEncodingException,
			SocketException, UnknownHostException {
		final UUID uuid = getMachineId();
		if (uuid != null) {
			return uuid.toString();
		} else {
			return null;
		}
	}

	/**
	 * Asserts that the current machine has the UUID.
	 * 
	 * @param uuid
	 *            expected
	 * @return true if the argument passed is the uuid of the current machine.
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws UnsupportedEncodingException
	 */
	public boolean assertUUID(final UUID uuid)
			throws UnsupportedEncodingException, SocketException,
			UnknownHostException {
		final UUID machineUUID = getMachineId();
		if (machineUUID == null) {
			return false;
		}
		return machineUUID.equals(uuid);
	}

	/**
	 * Asserts that the current machine has the UUID.
	 * 
	 * @param uuid
	 *            expected in String format
	 * @return true if the argument passed is the uuid of the current machine.
	 */
	public boolean assertUUID(final String uuid) {
		try {
			return assertUUID(java.util.UUID.fromString(uuid));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * A very simple main that prints out the machine UUID to the standard
	 * output.
	 * <p>
	 * This code takes into account the hardware address (Ethernet MAC) when
	 * calculating the hardware UUID.
	 * 
	 * @param args not used
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(final String[] args)
			throws UnsupportedEncodingException, SocketException,
			UnknownHostException {
		final HardwareBinder hb = new HardwareBinder();
		System.out.print(hb.getMachineIdString());
	}
}
