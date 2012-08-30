/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.verhas.licensor;

import java.net.NetworkInterface;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.digests.MD5Digest;

/**
 * The hardware binder binds a license to a certain hardware.
 * The use of this feature is optional. Calling methods from this class
 * the license manager can check that the license is deployed on the machine
 * it was destined and may decide NOT to work on other machines than it was
 * destined to.
 * <p>
 * It is recommended that such a checking is used only with warning purposes
 * and not treated as a strict license violation. It may happen that the
 * ethernet card of a server is replaced due to some failure and there is
 * no time to request a new license.
 * <p>
 * Therefore it is a recommended practice to note the disalignment of the
 * license and send it to the log, but do not deter the operation of the
 * software.
 *
 * @author Peter Verhas <peter@verhas.com>
 */
public class HardwareBinder {

  private static Logger logger = Logger.getLogger(HardwareBinder.class);

  private class NetInt {

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
   * relies on the hardware addresses and the names of the network cards if the
   * software runs on Java6 or later.
   * <p>
   * To do so call this method before calling any other uuid calculating
   * method.
   * <p>
   * If the code runs on Java5 then the algorith automatically falls back to
   * ignoring the mac addresses and thus may generate different ID than
   * when it is run under Java6.
   */
  public void setUseHwAddress() {
    this.useHwAddress = true;
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
   * enumerated and their parameters are taken into account. Under Java5
   * the names of the network interfaces are used. With Java6 the names
   * and the hardware addresses are used.
   * <p>
   * If you want to ignore the network when generating the uuid then
   * call this method before any uuid calculating methos.
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

  private void calculateNetworkDigest(MD5Digest md5) {
    try {
      // count the interfaces first
      Enumeration<NetworkInterface> nis =
              java.net.NetworkInterface.getNetworkInterfaces();
      int numberOfInterfaces = 0;
      while (nis.hasMoreElements()) {
        NetworkInterface ni = nis.nextElement();
        try {
          if (!useHwAddress) {
            throw new RuntimeException("Forced Java5 fall-back");
          }
          if (!ni.isLoopback() && !ni.isVirtual() && !ni.isPointToPoint()) {
            numberOfInterfaces++;
          }
        } catch (Throwable e) {
          logger.debug("Probably java 5", e);
          numberOfInterfaces++;
        }
      }
      // allocate the array for the interface properties
      NetInt[] niarr = new NetInt[numberOfInterfaces];
      int index = 0;
      // collect the interface properties
      nis = java.net.NetworkInterface.getNetworkInterfaces();
      while (nis.hasMoreElements()) {
        NetworkInterface ni = nis.nextElement();
        try {
          if (!useHwAddress) {
            throw new RuntimeException("Forced Java5 fall-back");
          }
          if (!ni.isLoopback() && !ni.isVirtual() && !ni.isPointToPoint()) {
            niarr[index] = new NetInt();
            niarr[index].name = ni.getName();
            niarr[index].hwAddress = ni.getHardwareAddress();
            index++;
          }
        } catch (Throwable e) {
          logger.debug("Probably java 5", e);
          niarr[index] = new NetInt();
          niarr[index].hwAddress = null;
          niarr[index].name = ni.getName();
          index++;
        }
      }

      // SORT the network interfaces, do not rely on that non-guaranteed
      // feature that getNetworkInterfaces() returns the interfaces the
      // same order allways
      java.util.Arrays.sort(niarr, new Comparator() {

        public int compare(Object o1, Object o2) {
          NetInt a, b;
          a = (NetInt) o1;
          b = (NetInt) o2;
          return a.name.compareTo(b.name);
        }
      });

      for (NetInt ni : niarr) {
        md5.update(ni.name.getBytes("utf-8"), 0, ni.name.getBytes(
                "utf-8").length);
        if (ni.hwAddress != null) {
          md5.update(ni.hwAddress, 0, ni.hwAddress.length);
        }
      }
    } catch (Exception e) {
      logger.error("Exception while calulating network uuid", e);
      return;
    }
  }

  /**
   * Calculate the UUID for the machine this code is running on.
   * To do this the method lists all network interfaces that are
   * real 'server' interfaces (ignoring loop-back, virtual, and
   * point-to-point interfaces). The method takes each interface name
   * (as a string) and hardware address into a MD5 digest one after the
   * other and finally converts the resulting 128bit digest into a UUID.
   * <p>
   * The method also feeds the local machine name into the digest.
   * <p>
   * This method relies on Java 6 methods, but also works with Java 5.
   * However the result will not be the same on Java 5 as on Java 6.
   * @return the UUID of the machine or null if the uuid can not be calculated.
   */
  public UUID getMachineId() {
    try {
      MD5Digest md5 = new MD5Digest();
      md5.reset();
      if (useNetwork) {
        calculateNetworkDigest(md5);
      }
      if (useHostName) {
        // feed the hostname into the digest
        String hostName = java.net.InetAddress.getLocalHost().
                getHostName();
        md5.update(hostName.getBytes("utf-8"), 0,
                hostName.getBytes("utf-8").length);
      }
      if (useArchitecture) {
        String arch = System.getProperty("os.arch");
        md5.update(arch.getBytes("utf-8"), 0,
                arch.getBytes("utf-8").length);
      }
      byte[] digest = new byte[16];
      md5.doFinal(digest, 0);
      // convert the MD5 to UUID and return
      return UUID.nameUUIDFromBytes(digest);
    } catch (Exception ex) {
      logger.error("Can not get hardware uuid: ", ex);
      return null;
    }
  }

  /**
   * Get the machine id as an UUID string.
   * @return the UUID as a string
   */
  public String getMachineIdString() {
    UUID uuid = getMachineId();
    if (uuid != null) {
      return uuid.toString();
    } else {
      return null;
    }
  }

  /**
   * Asserts that the current machine has the UUID.
   * @param uuid expected
   * @return true if the argument passed is the uuid of the current machine.
   */
  public boolean assertUUID(UUID uuid) {
    UUID machineUUID = getMachineId();
    if (machineUUID == null) {
      return false;
    }
    return machineUUID.equals(uuid);
  }

  /**
   * Asserts that the current machine has the UUID.
   * @param uuid expected in String format
   * @return true if the argument passed is the uuid of the current machine.
   */
  public boolean assertUUID(String uuid) {
    return assertUUID(java.util.UUID.fromString(uuid));
  }

  /**
   * A very simple main that prints out the machine UUID to the standard output.
   * <p>
   * This code takes into account the hardware address (Ethernet MAC) when
   * calculating the hardware UUID.
   * @param args
   */
  public static void main(String[] args) {
    HardwareBinder hb = new HardwareBinder();
    hb.setUseHwAddress();
    System.out.print(hb.getMachineIdString());
  }
}
