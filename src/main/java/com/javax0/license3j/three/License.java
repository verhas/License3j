package com.javax0.license3j.three;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A license describes the rights that a certain user has. The rights are represented by {@link Feature}s.
 * Each feature has a name, type and a value. The license is essentially the set of features.
 * <p>
 * As examples features can be license expiration date and time, number of users allowed to use the software,
 * name of rights and so on.
 */
public class License {
    private static final int MAGIC = 0x21CE_4E_5E; // LICE(N=4E)SE
    private static final String SIGNATURE_KEY = "licenseSignature";
    private static final String DIGEST_KEY = "signatureDigest";
    final private Map<String, Feature> features = new HashMap<>();

    public License() {
    }

    protected License(License license) {
        features.putAll(license.features);
    }

    public Feature get(String name) {
        return features.get(name);
    }

    public void sign(PrivateKey key, String digest) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        add(Feature.Create.stringFeature(DIGEST_KEY, digest));
        final var digester = MessageDigest.getInstance(digest);
        final var ser = unsigned();
        final var digestValue = digester.digest(ser);
        final var cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final var signature = cipher.doFinal(digestValue);
        add(signature);
    }

    public boolean isOK(PublicKey key) {
        try {
            final var digester = MessageDigest.getInstance(get(DIGEST_KEY).getString());
            final var ser = unsigned();
            final var digestValue = digester.digest(ser);
            final var cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key);
            final var sigDigest = cipher.doFinal(getSignature());
            return Arrays.equals(digestValue, sigDigest);
        } catch (Exception e) {
            return false;
        }
    }

    public Feature add(Feature feature) {
        if (feature.name().equals(SIGNATURE_KEY) && !feature.isBinary()) {
            throw new IllegalArgumentException("Signature of a license has to be binary.");
        }
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
     * <p>
     * when the value is multiline then it is converted to be multiline. Multiline strings are represented as usually
     * in unix, whith the HERE_STRING. The value in this case starts right after the {@code =} character with {@code <<}
     * characters and a string to the end of the line that does not appear as a single line inside the string value.
     * This value signals the end of the string and all other lines before it is part of the multiline string.
     * For example:
     *
     * <pre>
     *     feature name : STRING =&lt;&lt;END
     *   this is
     *     a multi-line
     *       string
     * END
     * </pre>
     * <p>
     * In this example the string {@code END} singals the end of the string and the lines between are part of the
     * strings. A feature string is also converted to multi-line representation if it happens to start with the
     * characters {@code &lt;&lt;}.
     * <p>
     * The generated string can be used as argument to {@link Create#from(String)}.
     *
     * @return the converted license as a string
     */
    @Override
    public String toString() {
        final var sb = new StringBuilder();
        Feature[] features = featuresSorted(Set.of());
        for (Feature feature : features) {
            final var valueString = feature.valueString();
            final String value =
                    valueString.contains("\n") || valueString.startsWith("<<")
                            ? multilineValueString(valueString) : valueString;
            sb.append(feature.toStringWith(value)).append("\n");
        }
        return sb.toString();
    }

    private Feature[] featuresSorted(Set<String> excluded) {
        Feature[] features = this.features.values().stream().filter(f -> !excluded.contains(f.name()))
                .sorted(Comparator.comparing(Feature::name)).collect(Collectors.toList()).toArray(new Feature[0]);
        return features;
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
        return serialized(Set.of());
    }

    public byte[] unsigned() {
        return serialized(Set.of(SIGNATURE_KEY));
    }

    public void add(byte[] signature) {
        add(Feature.Create.binaryFeature(SIGNATURE_KEY, signature));
    }

    public byte[] getSignature() {
        return get(SIGNATURE_KEY).getBinary();
    }

    private byte[] serialized(Set<String> excluded) {
        Feature[] includedFeatures = featuresSorted(excluded);
        final var featureNr = includedFeatures.length;
        byte[][] featuresSerialized = new byte[featureNr][];
        var i = 0;
        var size = 0;
        for (final var feature : includedFeatures) {
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
            try( var reader = new BufferedReader(new StringReader(text))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final var parts = Feature.splitString(line);
                    final var name = parts[0];
                    final var typeString = parts[1];
                    final var valueString = getValueString(reader, parts[2]);
                    license.add(Feature.getFeature(name, typeString, valueString));
                }
                return license;
            }catch(IOException e){
                throw new IllegalArgumentException(e);
            }
        }

        private static String getValueString(BufferedReader reader, String valueString) throws IOException {
            if (valueString.startsWith("<<")) {
                final var endLine = valueString.substring(2).trim();
                final var sb = new StringBuilder();
                String valueNextLine;
                while ((valueNextLine=reader.readLine()) != null ) {
                    if (valueNextLine.trim().equals(endLine)) {
                        break;
                    } else {
                        sb.append(valueNextLine);
                    }
                }
                valueString = sb.toString();
            }
            return valueString;
        }
    }
}
