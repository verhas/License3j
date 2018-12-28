package com.javax0.license3j;

import com.javax0.license3j.three.Feature;
import com.javax0.license3j.three.License;
import com.javax0.license3j.three.crypto.LicenseKeyPair;
import com.javax0.license3j.three.io.*;
import com.javax0.license3j.utils.ParameterParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommandLineApp {
    public static final String PUBLIC_KEY_FILE = "publicKeyFile";
    public static final String PRIVATE_KEY_FILE = "privateKeyFile";
    public static final String KEY_FILE = "keyFile";
    public static final String ALGORITHM = "algorithm";
    public static final String SIZE = "size";
    public static final String FORMAT = "format";
    public static final String TEXT = "TEXT";
    public static final String BINARY = "BINARY";
    public static final String BASE_64 = "BASE64";
    private List<String> errors = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private License license;
    private LicenseKeyPair keyPair;
    private Matcher matcher;
    private String line;
    private CommandDefinition[] commandDefinitions = {
        command("feature", ".+", this::feature, "name:TYPE=value"),
        command("licenseLoad", "(?:format=(TEXT|BINARY|BASE64)\\s+)(.+)|([^=]+)",
            this::loadLicense, "[format=TEXT*|BIINARY|BASE64] fileName"),
        command("saveLicense", "(?:format=(TEXT|BINARY|BASE64)\\s+)(.+)|([^=]+)",
            this::saveLicense, "[format=TEXT*|BINARY|BASE64] fileName"),
        command("loadPrivateKey", ".+", this::loadPrivateKey, "[format=BINARY|BASE64] keyFile=xxx"),
        command("loadPublicKey", ".+", this::loadPublicKey, "[format=BINARY|BASE64] keyFile=xxx"),
        command("sign", "(?:digest=(.*))?", this::sign, "[digest=SHA-512]"),
        command("verify", "^$", this::verify, ">>no argument<<"),
        command("generateKeys", ".+",
            this::generate, "[algorithm=RSA] [size=2048] [format=BINARY|BASE64] public=xxx private=xxx"),
        command("newLicense", "^$", this::newLicense, ">>no argument<<"),
        command("dump", "^$", this::dumpLicense, ">>no argument<<"),
        command("digestPublicKey", "^$", this::digestPublicKey, ">>no argument<<")

    };

    private static CommandDefinition command(String command, String regex, Runnable executor, String usage) {
        return new CommandDefinition(command, regex, executor, usage);
    }

    public void dumpLicense() {
        if (license == null) {
            error("There is no license to show.");
            return;
        }
        try {
            final var baos = new ByteArrayOutputStream();
            final var reader = new LicenseWriter(baos);
            reader.write(license, IOFormat.STRING);
            message("License:\n" + new String(baos.toByteArray(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            error("Error writing license file " + e);
        }

    }

    public void saveLicense() {
        if (license == null) {
            error("There is no license to save.");
            return;
        }
        try {
            final var reader = new LicenseWriter(getLicenseFileName());
            final var format = getLicenseFormat();
            switch (format) {
                case TEXT:
                    reader.write(license, IOFormat.STRING);
                    break;
                case BINARY:
                    reader.write(license, IOFormat.BINARY);
                    break;
                case BASE_64:
                    reader.write(license, IOFormat.BASE64);
                    break;
                default:
                    error("Invalid format to write the license: " + format);
                    return;
            }
            message("License was saved into the file " + new File(line).getAbsolutePath());
        } catch (IOException e) {
            error("Error writing license file " + e);
        }
    }

    public void loadPrivateKey() {
        if (keyPair != null && keyPair.getPair() != null && keyPair.getPair().getPrivate() != null) {
            message("Overriding old key from file");
        }
        final var pars = ParameterParser.parse(line, Set.of(FORMAT, KEY_FILE));
        final var keyFile = pars.get(KEY_FILE);
        if (keyFile == null) {
            messages = new ArrayList<>();
            error("keyFile has to be specified from where the key is loaded");
            return;
        }
        final var format = IOFormat.valueOf(pars.getOrDefault(FORMAT, BINARY).toUpperCase());
        try (final var reader = new KeyPairReader(keyFile)) {
            keyPair = merge(keyPair, reader.readPrivate(format));
            final var keyPath = new File(keyFile).getAbsolutePath();
            message("Private key loaded from" + keyPath);
        } catch (Exception e) {
            error("An exception occurred loading the key: " + e);
            e.printStackTrace();
        }
    }

    public void loadPublicKey() {
        if (keyPair != null && keyPair.getPair() != null && keyPair.getPair().getPrivate() != null) {
            message("Overriding old key from file");
        }
        final var pars = ParameterParser.parse(line, Set.of(FORMAT, KEY_FILE));
        final var keyFile = pars.get(KEY_FILE);
        if (keyFile == null) {
            messages = new ArrayList<>();
            error("keyFile has to be specified from where the key is loaded");
            return;
        }
        final var format = IOFormat.valueOf(pars.getOrDefault(FORMAT, BINARY).toUpperCase());
        try (final var reader = new KeyPairReader(keyFile)) {
            keyPair = merge(keyPair, reader.readPublic(format));
            final var keyPath = new File(keyFile).getAbsolutePath();
            message("Public key loaded from" + keyPath);
        } catch (Exception e) {
            error("An exception occurred loading the keys: " + e);
            e.printStackTrace();
        }
    }

    private LicenseKeyPair merge(LicenseKeyPair oldKp, LicenseKeyPair newKp) {
        if (oldKp == null) {
            return newKp;
        }
        if (newKp.getPair().getPublic() != null) {
            return LicenseKeyPair.Create.from(newKp.getPair().getPublic(), oldKp.getPair().getPrivate());
        }
        if (newKp.getPair().getPrivate() != null) {
            return LicenseKeyPair.Create.from(oldKp.getPair().getPublic(), newKp.getPair().getPrivate());
        }
        return oldKp;
    }

    public void digestPublicKey() {
        try {
            if (keyPair == null) {
                error("There is no public key loaded");
                return;
            }
            final var key = keyPair.getPair().getPublic().getEncoded();
            final var md = MessageDigest.getInstance("SHA-512");
            final var calculatedDigest = md.digest(key);
            final var javaCode = new StringBuilder("--KEY RING DIGEST START\nbyte [] digest = new byte[] {\n");
            for (int i = 0; i < calculatedDigest.length; i++) {
                int intVal = ((int) calculatedDigest[i]) & 0xff;
                javaCode.append(String.format("(byte)0x%02X, ", intVal));
                if (i % 8 == 0) {
                    javaCode.append("\n");
                }
            }
            javaCode.append("\n};\n---KEY RING DIGEST END\n");
            message("\n" + javaCode.toString());
        } catch (NoSuchAlgorithmException e) {
            error("" + e);
        }
    }

    public void generate() {
        if (keyPair != null) {
            error("Cannot generate key pair when there are already keys.");
            return;
        }
        final var pars = ParameterParser.parse(line, Set.of(ALGORITHM, SIZE, FORMAT,
            PUBLIC_KEY_FILE, PRIVATE_KEY_FILE));
        final var algorithm = pars.getOrDefault(ALGORITHM, "RSA");
        final var sizeString = pars.getOrDefault(SIZE, "2048");
        final var format = IOFormat.valueOf(pars.getOrDefault(FORMAT, BINARY));
        final var publicKeyFile = pars.get(PUBLIC_KEY_FILE);
        final var privateKeyFile = pars.get(PRIVATE_KEY_FILE);
        if (publicKeyFile == null || privateKeyFile == null) {
            error("Keypair generation needs output files specified where keys are to be saved. " +
                "Use options 'publicKeyFile' and 'privateKeyFile'");
            return;
        }
        final int size;
        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException e) {
            error("Option size has to be a positive decimal integer value. " +
                sizeString + " does not qualify as such.");
            return;
        }
        generateKeys(algorithm, size);
        try (final var writer = new KeyPairWriter(privateKeyFile, publicKeyFile)) {
            writer.write(keyPair, format);
            final var privateKeyPath = new File(privateKeyFile).getAbsolutePath();
            message("Private key saved to " + privateKeyPath);
            message("Public key saved to " + new File(publicKeyFile).getAbsolutePath());
        } catch (IOException e) {
            error("An exception occurred saving the keys: " + e);
        }
    }

    public void verify() {
        if (license.isOK(keyPair.getPair().getPublic())) {
            message("License is properly signed.");
        } else {
            error("License is not signed properly.");
        }
    }

    public void sign() {
        try {
            final var digest = matcher.group(1) == null ? "SHA-512" : matcher.group(1);
            license.sign(keyPair.getPair().getPrivate(), digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void feature() {
        if (license == null) {
            error("Feature can not be added when there is no license loaded. Use 'loadLicense' or 'newLicense'");
            return;
        }
        license.add(Feature.Create.from(line));
    }

    public void newLicense() {
        license = new License();
    }

    public void loadLicense() {
        try {
            final LicenseReader reader = new LicenseReader(getLicenseFileName());
            final String format = getLicenseFormat();
            switch (format) {
                case TEXT:
                    license = reader.read(IOFormat.STRING);
                    break;
                case BINARY:
                    license = reader.read(IOFormat.BINARY);
                    break;
                case BASE_64:
                    license = reader.read(IOFormat.BASE64);
                    break;
                default:
                    error("Invalid format to read the license: " + format);
            }
        } catch (IOException e) {
            error("Error reading license file " + e);
        }
    }

    private String getLicenseFormat() {
        return matcher.group(1) == null ? TEXT : matcher.group(1);
    }

    private String getLicenseFileName() {
        return matcher.group(matcher.group(1) == null ? 3 : 2);
    }

    private void generateKeys(String algorithm, int size) {
        try {
            keyPair = LicenseKeyPair.Create.from(algorithm, size);
        } catch (NoSuchAlgorithmException e) {
            error("Algorithm " + algorithm + " is not handled by current version of this application.");
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    private void error(String s) {
        errors.add(s);
    }

    public List<String> getMessages() {
        return messages;
    }

    private void message(String s) {
        messages.add(s);
    }


    public String usage() {
        final var sb = new StringBuilder();
        sb.append("Usage: keyword options keyword options ... \n");
        sb.append("Commands implemented:\n");
        for (var command : commandDefinitions) {
            sb.append("    ").append(command.keyword).append(" ").append(command.usage).append("\n");
        }
        return sb.toString();
    }

    public void execute(String line) {
        errors = new ArrayList<>();
        messages = new ArrayList<>();
        final var trimmedLine = line.trim();
        final var words = trimmedLine.split("\\s+");
        if (words.length == 0) {
            return;
        }
        final var keyword = words[0];
        this.line = trimmedLine.substring(keyword.length()).trim();
        CommandDefinition cd = null;
        for (final var command : commandDefinitions) {
            if (command.keyword.startsWith(keyword)) {
                if (cd == null) {
                    cd = command;
                } else {
                    error("command '" + keyword + "' is ambiguous");
                    return;
                }
            }
        }
        if (cd == null) {
            error("command '" + keyword + "' is not defined");
            return;
        }
        matcher = cd.regex.matcher(this.line);
        if (!matcher.matches()) {
            error("command '" + line + "' syntax is erroneous");
            error("usage: " + cd.keyword + " " + cd.usage);
            return;
        }
        cd.executor.run();
    }

    public void printApplicationState() {
        if (license == null) {
            Repl.say("License is not loaded.");
        } else {
            final var owner = license.get("owner");
            if (owner == null || !owner.isString()) {
                Repl.say("License loaded.");
            } else {
                Repl.say("License of '%s' is loaded", owner.getString());
            }
        }
        if (keyPair == null) {
            Repl.say("Keys are not loaded");
        } else {
            var priv = keyPair.getPair().getPrivate();
            var publ = keyPair.getPair().getPublic();
            if (priv != null) {
                Repl.say("Private key loaded");
            }
            if (publ != null) {
                Repl.say("Public key loaded");
            }
        }
    }

    private static final class CommandDefinition {
        final String keyword;
        final Pattern regex;
        final Runnable executor;
        final String usage;

        private CommandDefinition(String keyword, String regex, Runnable executor, String usage) {
            this.keyword = keyword;
            this.regex = Pattern.compile(regex);
            this.executor = executor;
            this.usage = usage;
        }
    }

}
