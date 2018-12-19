package com.javax0.license3j;

import com.javax0.license3j.three.Feature;
import com.javax0.license3j.three.License;
import com.javax0.license3j.three.crypto.LicenseKeyPair;
import com.javax0.license3j.three.io.IOFormat;
import com.javax0.license3j.three.io.KeyPairWriter;
import com.javax0.license3j.three.io.LicenseReader;
import com.javax0.license3j.three.io.LicenseWriter;
import com.javax0.license3j.utils.CommandLineProcessor;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandLineApp {
    private CommandLineProcessor commandLine;
    private List<String> errors = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private License license;
    private LicenseKeyPair keyPair;

    public CommandLineApp(String[] args) {
        reset();
        commandLine.process(args);
    }

    public static void saveLicense(CommandLineApp it) {
        if (it.license == null) {
            it.error("There is no license to save.");
            return;
        }
        final var outputFile = it.commandLine.option("licenseFile");
        if (!outputFile.isPresent()) {
            it.error(Commands.SAVE_LICENSE.text() + " needs output file specified using the option 'licenseFile'");
            return;
        }
        try {
            final var reader = new LicenseWriter(outputFile.get());
            final var format = it.commandLine.option("outputFormat").orElse("text");
            switch (format) {
                case "text":
                    reader.write(it.license, IOFormat.STRING);
                    break;
                case "binary":
                    reader.write(it.license, IOFormat.BINARY);
                    break;
                case "base64":
                    reader.write(it.license, IOFormat.BASE64);
                    break;
                default:
                    it.error("Invalid format to write the license: " + format);
                    return;
            }
            it.message("License was saved into the file " + new File(outputFile.get()).getAbsolutePath());
        } catch (IOException e) {
            it.error("Error writing license file " + e);
        }
    }

    public static void generate(CommandLineApp it) {
        if (it.keyPair != null) {
            it.error("Cannot generate key pair when there are already keys.");
            return;
        }
        final var algorithm = it.commandLine.option("algorithm").orElse("RSA");
        final var sizeString = it.commandLine.option("size").orElse("2048");
        final var format = it.commandLine.option("format").orElse("binary");
        final var publicKeyFile = it.commandLine.option("publicKeyFile");
        final var privateKeyFile = it.commandLine.option("privateKeyFile");
        if (!publicKeyFile.isPresent() || !privateKeyFile.isPresent()) {
            it.error("Keypair generation needs output files specified where keys are to be saved. " +
                    "Use options 'publicKeyFile' and 'privateKeyFile'");
            return;
        }
        final int size;
        try {
            size = Integer.parseInt(sizeString);
        } catch (NumberFormatException e) {
            it.error("Option size has to be a positive decimal integer value. " +
                    sizeString + " does not qualify as such.");
            return;
        }
        it.generateKeys(algorithm, size);
        try (final var writer = new KeyPairWriter(privateKeyFile.get(), publicKeyFile.get())) {
            writer.write(it.keyPair, format);
            final var privateKeyPath = new File(privateKeyFile.get()).getAbsolutePath();
            it.message("Private key saved to " + privateKeyPath);
            it.message("Public key saved to " + new File(publicKeyFile.get()).getAbsolutePath());
        } catch (IOException e) {
            it.error("An exception occured saving the keys: " + e);
        }
    }

    public static void verify(CommandLineApp it) {
        if( it.license.isOK(it.keyPair.getPair().getPublic())){
            it.message("License is properly signed.");
        }else{
            it.error("License is not signed properly.");
        }
    }

    public static void sign(CommandLineApp it) {
        try {
            it.license.sign(it.keyPair.getPair().getPrivate(), "SHA-512");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void feature(CommandLineApp it) {
        if (it.license == null) {
            it.error("Feature can not be added when there is no license loaded. Use 'loadLicense' or 'newLicense'");
            return;
        }
        final var value = it.commandLine.option("value");
        if (!value.isPresent()) {
            it.error("to add a feature you have to specify the value of it. Use the option 'value'");
            return;
        }
        it.license.add(Feature.Create.from(value.get()));
    }

    public static void newLicense(CommandLineApp it) {
        it.license = new License();
    }

    public static void loadLicense(CommandLineApp it) {
        final var inputFile = it.commandLine.option("input");
        if (!inputFile.isPresent()) {
            it.error(Commands.LOAD_LICENSE.text() + " needs input file specified using the option 'input'");
            return;
        }
        try {
            final var reader = new LicenseReader(inputFile.get());
            final var format = it.commandLine.option("inputFormat").orElse("text");
            switch (format) {
                case "text":
                    it.license = reader.read(IOFormat.STRING);
                    break;
                case "binary":
                    it.license = reader.read(IOFormat.BINARY);
                    break;
                case "base64":
                    it.license = reader.read(IOFormat.BASE64);
                    break;
                default:
                    it.error("Invalid format to read the license: " + format);
            }
        } catch (IOException e) {
            it.error("Error reading license file " + e);
        }
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

    private void reset() {
        commandLine =
                new CommandLineProcessor(
                        Arrays.stream(Commands.values()).map(Commands::text).toArray(String[]::new),
                        new String[]{
                                "input",
                                "licenseFile",
                                "inputFormat",
                                "outputFormat",
                                "privateKeyFile",
                                "publicKeyFile",
                                "size",
                                "algorithm",
                                "value"
                        });
        errors = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public String usage() {
        final var sb = new StringBuilder();
        sb.append("Usage: command options command options ... \n");
        sb.append("Commands implemented:\n");
        for (var command : commandLine.allCommands()) {
            sb.append("    " + command + "\n");
        }
        sb.append("Options implemented:\n");
        for (var option : commandLine.allOptions()) {
            sb.append("    " + option + "\n");
        }
        return sb.toString();
    }

    public void execute(String[] args) {
        reset();
        commandLine.process(args);
        execute();
        printApplicationState();
    }

    private void printApplicationState() {
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

    public void execute() {
        for (int i = 0; i < commandLine.getCommands().size(); i++) {
            final var commandString = commandLine.getCommands().get(i);
            for (final var command : Commands.values()) {
                if (command.text().equals(commandString)) {
                    command.command.accept(this);
                }
            }
        }
    }

    private enum Commands {
        LOAD_LICENSE("loadLicense", CommandLineApp::loadLicense),
        SIGN("sign", CommandLineApp::sign),
        VERIFY("verify", CommandLineApp::verify),
        GENERATE("generate", CommandLineApp::generate),
        SAVE_LICENSE("saveLicense", CommandLineApp::saveLicense),
        FEATURE("feature", CommandLineApp::feature),
        NEW_LICENSE("newLicense", CommandLineApp::newLicense);
        private final String text;
        private final Consumer<CommandLineApp> command;

        Commands(String text, Consumer<CommandLineApp> command) {
            this.text = text;
            this.command = command;
        }

        String text() {
            return text;
        }
    }


}
