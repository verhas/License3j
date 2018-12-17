package com.javax0.license3j.three;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class LicenseTest {

    @Test
    public void createLicenseViaAPI(){
        final var sut = new License();
        final var now = new Date(1545047719295L);
        sut.add(Feature.Create.stringFeature("owner","Peter Verhas"));
        sut.add(Feature.Create.stringFeature("title","A license test, \ntest license"));
        sut.add(Feature.Create.dateFeature("expiry", now));
        byte[] buffer = sut.serialized();
        final var restored = License.Create.from(buffer);
        Assertions.assertEquals("Peter Verhas",restored.get("owner").getString());
        Assertions.assertEquals(now, restored.get("expiry").getDate());
        Assertions.assertEquals("owner:STRING=Peter Verhas\n" +
                "expiry:DATE=2018-12-17 12:55:19.295\n" +
                "title:STRING=<<B\n" +
                "A license test, \n" +
                "test license\n" +
                "B\n",sut.toString());
    }
}
