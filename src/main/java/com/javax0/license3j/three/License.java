package com.javax0.license3j.three;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * A license describes the rights that a certain user has. The rights are represented by {@link Feature}s.
 * Each feature has a name, type and a value. The license is essentially the set of features.
 * <p>
 * As examples features can be license expiration date and time, number of users allowed to use the software,
 * name of rights and so on.
 */
public class License {
    final private Map<String, Feature> features = new HashMap<>();

    public Feature get(String name) {
        return features.get(name);
    }

    public Feature put(String name, Feature feature) {
        return features.put(name, feature);
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

        final var buffer = ByteBuffer.allocate(size + Integer.BYTES * featureNr);
        for (final var featureSerialized : featuresSerialized) {
            buffer.putInt(featureSerialized.length);
            buffer.put(featureSerialized);
        }
        return buffer.array();
    }

    public static class Create {
        public static License from(final byte[] array) {
            var license = new License();
            var buffer = ByteBuffer.wrap(array);
            while (buffer.hasRemaining()) {
                try {
                    var featureLength = buffer.getInt();
                    var featureSerialized = new byte[featureLength];
                    buffer.get(featureSerialized);
                    var feature = Feature.Create.from(featureSerialized);
                    license.put(feature.name(), feature);
                } catch (BufferUnderflowException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return license;
        }
    }

}
