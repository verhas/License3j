package com.javax0.license3j.three;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A license describes the rights that a certain user has. The rights are represented by {@link Feature}s.
 * Each feature has a name, type and a value. The license is essentially the set of features.
 * <p>
 * As examples features can be license expiration date and time, number of users allowed to use the software,
 * name of rights and so on.
 */
public class License {
    private static final int MAGIC = 0x21CE_4E_5E; // LICE(N=4E)SE
    final private Map<String, Feature> features = new HashMap<>();

    public Feature get(String name) {
        return features.get(name);
    }

    public Feature add(Feature feature) {
        return features.put(feature.name(), feature);
    }

    /**
     * Converts a license to string. The string contains all the features and also the order of the
     * features are guaranteed to be always the same.
     * <p>
     * Every feature is formatted as
     * <pre>
     *     name:TYPE=value
     * </pre>
     *
     * when the value is multiline then it is converted to be multiline. Multiline strings are represented as usually
     * in unix, whith the HERE_STRING. The value in this case starts right after the {@code =} character with {@code <<}
     * characters and a string to the end of the line that does not appear as a single line inside the string value.
     * This value signals the end of the string and all other lines before it is part of the multiline string.
     * For example:
     *
     * <pre>
     *     feature name : STRING =<<END
     *   this is
     *     a multi-line
     *       string
     * END
     * </pre>
     *
     * In this example the string {@code END} singals the end of the string and the lines between are part of the
     * strings. A feature string is also converted to multi-line representation if it happens to start with the
     * characters {@code <<}.
     * <p>
     * The generated string can be used as argument to {@link Create#from(String)}.
     *
     * @return the converted license as a string
     */
    @Override
    public String toString() {
        final var sb = new StringBuilder();
        Feature[] features = this.features.values().toArray(new Feature[0]);
        Arrays.sort(features, Comparator.comparing(Feature::name));
        for (Feature feature : features) {
            final var valueString = feature.valueString();
            final String value =
                valueString.contains("\n") || valueString.startsWith("<<")
                    ? multilineValueString(valueString) : valueString;
            sb.append(feature.toStringWith(value)).append("\n");
        }
        return sb.toString();
    }


    private String multilineValueString(String s) {
        List<String> lines = new ArrayList<>(List.of(s.split("\n")));
        final var sb = new StringBuilder();
        var i = 0;
        for (final var line : lines) {
            sb.append(line.length() <= i || line.charAt(i) == 'A' ? 'B' : 'A');
            i++;
        }
        final var delimiter = sb.toString();
        String shortDelimiter = null;
        for (int j = 1; j < delimiter.length(); j++) {
            if (!lines.contains(delimiter.substring(0, j))) {
                shortDelimiter = delimiter.substring(0, j);
                break;
            }
        }
        lines.add(0, "<<" + shortDelimiter);
        lines.add(shortDelimiter);
        return String.join("\n", lines);
    }

    public byte[] serialized() {
        final var featureNr = features.values().size();
        byte[][] featuresSerialized = new byte[featureNr][];
        var i = 0;
        var size = 0;
        for (final var feature : features.values()) {
            featuresSerialized[i] = feature.serialized();
            size += featuresSerialized[i].length;
            i++;
        }

        final var buffer = ByteBuffer.allocate(size + Integer.BYTES * (featureNr + 1));
        buffer.putInt(MAGIC);
        for (final var featureSerialized : featuresSerialized) {
            buffer.putInt(featureSerialized.length);
            buffer.put(featureSerialized);
        }
        return buffer.array();
    }

    public static class Create {
        public static License from(final byte[] array) {
            if (array.length < Integer.BYTES) {
                throw new IllegalArgumentException("serialized license is too short");
            }
            final var license = new License();
            final var buffer = ByteBuffer.wrap(array);
            final var magic = buffer.getInt();
            if (magic != MAGIC) {
                throw new IllegalArgumentException("serialized license is corrupt");
            }
            while (buffer.hasRemaining()) {
                try {
                    final var featureLength = buffer.getInt();
                    final var featureSerialized = new byte[featureLength];
                    buffer.get(featureSerialized);
                    final var feature = Feature.Create.from(featureSerialized);
                    license.add(feature);
                } catch (BufferUnderflowException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return license;
        }

        /**
         * Get a license with the features from the string. The format of the string is the same as the one that
         * was generated by the {@link License#toString()}.
         * <p>
         * The syntax is more relaxed than in case of {@link License#toString()}, however. The spaces at the start
         * of the lines, before the : and the type name and the = sign are removed. In case of multi-line string
         * the spaces before and after the end string are removed.
         *
         * @param text the license in String format
         * @return the license
         */
        public static License from(final String text) {
            final var license = new License();
            String[] lines = text.split("\n");
            int i = 0;
            String line = lines[i++];
            while (i < lines.length) {
                final var parts = Feature.splitString(line);
                final var name = parts[0];
                final var typeString = parts[1];
                var valueString = parts[2];
                if (valueString.startsWith("<<")) {
                    final var endLine = valueString.substring(2).trim();
                    final var sb = new StringBuilder();
                    while (i < lines.length) {
                        final var valueNextLine = lines[i++];
                        if (valueNextLine.trim().equals(endLine)) {
                            break;
                        } else {
                            sb.append(valueNextLine);
                        }
                    }
                    valueString = sb.toString();
                }
                license.add(Feature.getFeature(name, typeString, valueString));
                if (i < lines.length) {
                    line = lines[i++];
                }
            }
            return license;
        }
    }

}
