package com.javax0.license3j;

import com.javax0.license3j.three.License;
import com.javax0.license3j.three.io.IOFormat;
import com.javax0.license3j.three.io.LicenseReader;
import com.javax0.license3j.utils.CommandLineProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandLineApp {
    private List<String> errors = new ArrayList<>();

    private enum Commands {
        LOAD_LICENSE("loadLicense", CommandLineApp::loadLicense),
        SIGN("sign", CommandLineApp::sign),
        VERIFY("verify", CommandLineApp::verify),
        GENERATE("generate", CommandLineApp::generate),
        SAVE_LICENSE("saveLicense", CommandLineApp::saveLicense);

        private final String text;
        private final Consumer<CommandLineApp> command;

        String text() {
            return text;
        }

        Commands(String text, Consumer<CommandLineApp> command) {
            this.text = text;
            this.command = command;
        }
    }

    private final CommandLineProcessor commandLine =
        new CommandLineProcessor(
            Arrays.stream(Commands.values()).map(Commands::text).toArray(String[]::new),
            new String[]{
                "input",
                "output",
                "inputFormat",
                "outputFormat",
                "privateKeyFile",
                "publicKeyFile",
                "size",
                "algorithm"
            });

    public CommandLineApp(String[] args) {
        commandLine.process(args);
    }

    private License license;

    public static void saveLicense(CommandLineApp it) {

    }

    public static void generate(CommandLineApp it) {

    }

    public static void verify(CommandLineApp it) {

    }

    public static void sign(CommandLineApp it) {

    }

    public static void loadLicense(CommandLineApp it) {
        final var inputFile = it.commandLine.option("input");
        if (inputFile.isEmpty()) {
            it.errors.add(Commands.LOAD_LICENSE.text() + " needs input file specified using the option 'input'");
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
                    it.errors.add("Invalid format to read the license: " + format);
            }
        } catch (IOException e) {
            it.errors.add("Error reading license file " + e);
        }
    }

    public List<String> execute() {

        for (int i = 0; i < commandLine.getCommands().size(); i++) {
            final var commandString = commandLine.getCommands().get(i);

            for (final var command : Commands.values()) {
                if (command.text().equals(commandString)) {
                    command.command.accept(this);
                }
            }

        }
        return errors;
    }


}
