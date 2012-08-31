package com.verhas.licensor;

import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.UUID;

import org.bouncycastle.crypto.digests.MD5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Peter Verhas <peter@verhas.com>
 */
public class HardwareBinder {

	private static Logger logger = LoggerFactory
			.getLogger(HardwareBinder.class);

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

	private boolean useHwAddress = false;

	/**
	 * By default the class does not take the hardware addresses into account.
	 * Anyway hardware address can only be queried only with Java6 or later.
	 * Binding software to a specific hardware address may also be annoying when
	 * customer has to replace an Ethernet card.
	 * <p>
	 * Even though it is possible using this class to create a machine ID that
	 * relies on the hardware addresses and the names of the network cards if
	 * the software runs on Java6 or later.
	 * <p>
	 * To do so call this method before calling any other uuid calculating
	 * method.
	 * <p>
	 * Note that versions following 1.0.3 do NOT support 1.5 anymore.
	 */
	public void setUseHwAddress() {
		useHwAddress = true;
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

	private void assertUseHwAddress() {
		if (!useHwAddress) {
			throw new RuntimeException("Forced Java5 fall-back");
		}
	}

	private int numberOfInterfaces() throws SocketException {
		final Enumeration<NetworkInterface> networkInterfaces = java.net.NetworkInterface
				.getNetworkInterfaces();
		int interfaceCounter = 0;
		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();
			assertUseHwAddress();
			if (weShouldUsedForTheCalculationThis(networkInterface)) {
				interfaceCounter++;
			}
		}
		return interfaceCounter;
	}

	private boolean weShouldUsedForTheCalculationThis(
			final NetworkInterface networkInterface) throws SocketException {
		return !networkInterface.isLoopback() && !networkInterface.isVirtual()
				&& !networkInterface.isPointToPoint();
	}

	private NetworkInterfaceData[] networkInterfaceData()
			throws SocketException {
		final NetworkInterfaceData[] networkInterfaceArray = new NetworkInterfaceData[numberOfInterfaces()];
		int index = 0;
		// collect the interface properties
		final Enumeration<NetworkInterface> networkInterfaces = java.net.NetworkInterface
				.getNetworkInterfaces();
		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();
			assertUseHwAddress();
			if (weShouldUsedForTheCalculationThis(networkInterface)) {
				networkInterfaceArray[index] = new NetworkInterfaceData(
						networkInterface);
				index++;
			}
		}
		return networkInterfaceArray;
	}

	/**
	 * SORT the network interfaces, do not rely on that non-guaranteed feature
	 * that getNetworkInterfaces() returns the interfaces the same order always
	 */
	private void sortNetworkInterfaces(final NetworkInterfaceData[] niarr) {
		Arrays.sort(niarr, new Comparator<NetworkInterfaceData>() {
			public int compare(final NetworkInterfaceData a, final NetworkInterfaceData b) {
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

	private void updateWithNetworkData(final MD5Digest md5) {
		try {
			final NetworkInterfaceData[] networkInterfaces = networkInterfaceData();
			sortNetworkInterfaces(networkInterfaces);
			updateWithNetworkData(md5, networkInterfaces);
		} catch (final Exception e) {
			logger.error("Exception while calulating network uuid", e);
			return;
		}
	}

	private void updateWithHostName(final MD5Digest md5) throws UnknownHostException,
			UnsupportedEncodingException {
		final String hostName = java.net.InetAddress.getLocalHost().getHostName();
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
	 */
	public UUID getMachineId() {
		try {
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
		} catch (final Exception ex) {
			logger.error("Can not get hardware uuid: ", ex);
			return null;
		}
	}

	/**
	 * Get the machine id as an UUID string.
	 * 
	 * @return the UUID as a string
	 */
	public String getMachineIdString() {
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
	 */
	public boolean assertUUID(final UUID uuid) {
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
		return assertUUID(java.util.UUID.fromString(uuid));
	}

	/**
	 * A very simple main that prints out the machine UUID to the standard
	 * output.
	 * <p>
	 * This code takes into account the hardware address (Ethernet MAC) when
	 * calculating the hardware UUID.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		final HardwareBinder hb = new HardwareBinder();
		hb.setUseHwAddress();
		System.out.print(hb.getMachineIdString());
	}
}
