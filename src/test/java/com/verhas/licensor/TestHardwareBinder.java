package com.verhas.licensor;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class TestHardwareBinder {

	@Test
	public void testMain() throws UnsupportedEncodingException, SocketException, UnknownHostException {
		HardwareBinder.main(null);
	}

	@Test
	public void testMachineId() throws UnsupportedEncodingException,
			SocketException, UnknownHostException {
		for (int i = 0; i < 8; i++) {
			HardwareBinder hb = new HardwareBinder();
			if ((i & 1) != 0)
				hb.ignoreNetwork();
			if ((i & 2) != 0)
				hb.ignoreArchitecture();
			if ((i & 3) != 0)
				hb.ignoreHostName();
			UUID uuid = hb.getMachineId();
			Assert.assertTrue(hb.assertUUID(uuid));

			String uuidS = hb.getMachineIdString();
			Assert.assertTrue(hb.assertUUID(uuidS));
			String buuid = uuidS + "*";
			Assert.assertFalse(hb.assertUUID(buuid));
			String lastChar = uuidS.substring(uuidS.length() - 1);
			if (lastChar.equals("f")) {
				lastChar = "e";
			} else {
				lastChar = "f";
			}
			uuidS = uuidS.substring(0, uuidS.length() - 1) + lastChar;
			Assert.assertFalse(hb.assertUUID(uuidS));
		}
	}

}
