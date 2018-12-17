package com.javax0.license3j.three;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        for (Feature feature : features.values()) {
            final var valueString = feature.valueString();
            final String value;
            if (valueString.contains("\n") || valueString.startsWith("<<")) {
                value = multilineValueString(valueString);
            } else {
                value = valueString;
            }
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
            var license = new License();
            var buffer = ByteBuffer.wrap(array);
            var magic = buffer.getInt();
            if (magic != MAGIC) {
                throw new IllegalArgumentException("serialized license is corrupt");
            }
            while (buffer.hasRemaining()) {
                try {
                    var featureLength = buffer.getInt();
                    var featureSerialized = new byte[featureLength];
                    buffer.get(featureSerialized);
                    var feature = Feature.Create.from(featureSerialized);
                    license.add(feature);
                } catch (BufferUnderflowException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return license;
        }
    }

}
