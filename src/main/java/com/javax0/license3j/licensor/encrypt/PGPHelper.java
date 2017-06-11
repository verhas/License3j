package com.javax0.license3j.licensor.encrypt;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.*;
import java.util.Date;
import java.util.Iterator;

public class PGPHelper {
    private int hashAlgorithm = PGPUtil.SHA512;
    private PGPSecretKey key = null;

    public static boolean keyIsAppropriate(PGPSecretKey key, String userId, String keyUserId
    ) {
        return key.isSigningKey() && (userId == null || userId.equals(keyUserId));
    }

    public void setKey(PGPSecretKey key) {
        this.key = key;
    }

    private PGPSignatureGenerator signatureGenerator(
            final String keyPassPhrase) throws PGPException {
        final PGPContentSignerBuilder csBuilder = new JcaPGPContentSignerBuilder(
                key.getPublicKey().getAlgorithm(), hashAlgorithm);
        final PGPSignatureGenerator generator = new PGPSignatureGenerator(
                csBuilder);
        final PGPPrivateKey privateKey = extractPGPPrivateKey(keyPassPhrase.toCharArray());

        init(generator, privateKey);
        return generator;
    }

    private PGPPrivateKey extractPGPPrivateKey(final char[] keyPassPhrase)
            throws PGPException {
        PGPDigestCalculatorProvider calcProvider = new JcaPGPDigestCalculatorProviderBuilder()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
        PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(
                calcProvider).setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPassPhrase);
        return key.extractPrivateKey(decryptor);
    }

    private void setHashedSubpackets(
            final PGPSignatureGenerator signatureGenerator) {
        final Iterator<String> it = key.getPublicKey().getUserIDs();
        if (it.hasNext()) {
            final PGPSignatureSubpacketGenerator generator = new PGPSignatureSubpacketGenerator();
            generator.setSignerUserID(false, it.next());
            signatureGenerator.setHashedSubpackets(generator.generate());
        }
    }

    private void init(final PGPSignatureGenerator signatureGenerator,
                      final PGPPrivateKey pgpPrivKey) throws PGPException {
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
        setHashedSubpackets(signatureGenerator);
    }

    private PGPCompressedDataGenerator compressedDataGenerator() {
        return new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
    }

    private void encode(final String licensePlain,
                       final PGPSignatureGenerator signatureGenerator,
                       OutputStream outputStream) throws IOException {

        final PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        final OutputStream literalDataStream = literalDataGenerator.open(
                outputStream, PGPLiteralData.BINARY, "licenseFileName-Ignored",
                new Date(), new byte[1024]);
        final InputStream fIn = new ByteArrayInputStream(
                licensePlain.getBytes("utf-8"));
        int ch;
        while ((ch = fIn.read()) >= 0) {
            literalDataStream.write(ch);
            signatureGenerator.update((byte) ch);
        }
        literalDataGenerator.close();
    }

    public void setHashAlgorithm(int hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public byte[] encodeLicense(final String keyPassPhraseString, final String licensePlain) throws IOException, PGPException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final OutputStream aos = new ArmoredOutputStream(baos);
        final PGPCompressedDataGenerator generator = compressedDataGenerator();
        final BCPGOutputStream output = new BCPGOutputStream(generator.open(aos));

        final PGPSignatureGenerator signatureGenerator = signatureGenerator(keyPassPhraseString);
        signatureGenerator.generateOnePassVersion(false).encode(output);
        encode(licensePlain, signatureGenerator, output);
        signatureGenerator.generate().encode(output);
        generator.close();
        aos.close();
        return baos.toByteArray();
    }
}
