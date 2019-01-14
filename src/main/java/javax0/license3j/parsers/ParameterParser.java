package javax0.license3j.parsers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ParameterParser {
    public static Map<String, String> parse(String line, Set<String> parameters) {
        final var map = new HashMap<String, String>();
        final var parts = line.split("\\s+");
        final var index = new AtomicInteger(0);
        for (final var part : parts) {
            final var eq = part.indexOf("=");
            if (eq == -1) {
                map.put(""+index.addAndGet(1), part);
            } else {
                final var key = part.substring(0, eq);
                final var value = part.substring(eq + 1);
                map.put(findIt(key,parameters), value);
            }
        }
        return map;
    }

    private static String findIt(String prefix, Set<String> set) {
        String found = null;
        for (final var s : set) {
            if (s.startsWith(prefix)) {
                if (found == null) {
                    found = s;
                } else {
                    throw new IllegalArgumentException("Parameters " + prefix + " is ambiguous. " +
                            "It matches both " + found + " and also " + s);
                }
            }
        }
        if( found == null ){
            throw new IllegalArgumentException(prefix + " is not an allowed parameter");
        }
        return found;
    }

}
