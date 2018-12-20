package com.javax0.license3j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class TestCommandRegexes {

    private static final Map<String, Pattern> regex = getRegexes();

    private static Map<String, Pattern> getRegexes() {
        try {
            final var map = new HashMap<String, Pattern>();
            var cla = new CommandLineApp();
            var field = cla.getClass().getDeclaredField("commandDefinitions");
            field.setAccessible(true);
            Object[] commands = (Object[]) field.get(cla);
            var clazz = Class.forName("com.javax0.license3j.CommandLineApp$CommandDefinition");
            var kwField = clazz.getDeclaredField("keyword");
            var reField = clazz.getDeclaredField("regex");
            kwField.setAccessible(true);
            reField.setAccessible(true);
            for (Object command : commands) {
                final var keyword = (String) kwField.get(command);
                final var regex = (Pattern) reField.get(command);
                map.put(keyword, regex);
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertOK(Pattern sut, String line) {
        final var matcher = sut.matcher(line);
        Assertions.assertTrue(matcher.matches(), line + " does not match " + sut);
    }

    private static void assertOK(Pattern sut, String line, String... params) {
        final var matcher = sut.matcher(line);
        Assertions.assertTrue(matcher.matches(), line + " does not match " + sut);
        int i = 1;
        for (final var param : params) {
            Assertions.assertEquals(param,matcher.group(i++));
        }
    }

    private static void assertNo(Pattern sut, String line) {
        final var matcher = sut.matcher(line);
        Assertions.assertFalse(matcher.matches(), line + " matches " + sut);
    }

    @Test
    public void loadLicense() {
        Pattern sut = regex.get("loadLicense");
        assertOK(sut, "fileName");
        assertOK(sut, "format=text fileName","text","fileName");
        assertOK(sut, "format=binary fileName","binary","fileName");
        assertOK(sut, "format=base64 fileName","base64","fileName");
        assertOK(sut, "fileName",null,null,"fileName");
        assertNo(sut, "format=base6 fileName");
    }

    @Test
    public void verfy() {
        Pattern sut = regex.get("verify");
        assertOK(sut, "");
        assertNo(sut, ".");
        assertNo(sut, "anything");
    }

}
