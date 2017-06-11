package com.javax0.license3j.licensor;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;


import org.junit.Assert;
import org.junit.Test;

public class TestHardwareBinder {

	@Test
	public void testMain() throws UnsupportedEncodingException,
			SocketException, UnknownHostException {
		HardwareBinder.main(null);
	}

	private static final boolean falseTrue[] = new boolean[] { false, true };

	@Test
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
					Assert.assertTrue(hb.assertUUID(uuid));
				}
			}
		}
	}

	@Test
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
					Assert.assertTrue(hb.assertUUID(uuidS));
				}
			}
		}
	}

	@Test
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
					Assert.assertFalse(hb.assertUUID(buuid));
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
	public void wrongUuisStringAssertsFalse()
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
					Assert.assertFalse(hb.assertUUID(uuid));
				}
			}
		}
	}
}
