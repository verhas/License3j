package javax0.repl;

import java.util.*;

public class ParameterParser {

    private final Map<String, String> keys = new HashMap<>();
    private final List<String> values = new ArrayList<>();

    public static ParameterParser parse(String line, Set<String> parameters) {
        final var it = new ParameterParser();
        final var parts = line.split("\\s+");
        for (final var part : parts) {
            final var eq = part.indexOf("=");
            if (eq == -1) {
                it.values.add(part);
            } else {
                final var key = part.substring(0, eq);
                final var value = part.substring(eq + 1);
                it.keys.put(findIt(key, parameters), value);
            }
        }
        return it;
    }

    private static String findIt(String prefix, Set<String> set) {
        final List<String> commandsFound = new ArrayList<>();
        for (final var s : set) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                commandsFound.add(s);
            }
        }
        if (commandsFound.size() == 1) {
            return commandsFound.get(0);
        }
        if (commandsFound.size() == 0) {
            throw new IllegalArgumentException(prefix + " is not an allowed parameter");
        }
        ;
        throw new IllegalArgumentException("Parameters " + prefix + " is ambiguous. " +
                "It matches " + String.join(",", commandsFound) + ".");
    }

    public String get(String key, Set<String> values) {
        return findIt(keys.get(key), values);
    }

    public String get(String key) {
        return keys.get(key);
    }

    public String get(int index) {
        return values.get(index);
    }

    public String getOrDefault(String key, String def) {
        return keys.getOrDefault(key, def);
    }

    public String getOrDefault(String key, String def, Set<String> values) {
        final var value = keys.get(key);
        if( value == null ){
            return def;
        }
        return get(key, values);
    }
}
