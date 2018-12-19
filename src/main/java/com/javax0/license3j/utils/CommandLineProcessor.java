package com.javax0.license3j.utils;

import java.util.*;

public class CommandLineProcessor {
    private final String[] commanda;
    private final String[] optiona;
    private final Map<String, Optional<String>> options = new HashMap<>();
    private final List<String> commands = new ArrayList<>();

    public String[] allCommands(){
        return commanda;
    }
    public String[] allOptions(){
        return optiona;
    }

    /**
     * Create a new command line processor object.
     *
     * @param commands all the possible commands
     * @param options  all the possible options
     */
    public CommandLineProcessor(String[] commands, String[] options) {
        this.commanda = commands;
        this.optiona = options;
    }

    /**
     * Get the options that were gathered by the method {@code process()}.
     *
     * @return the map containing the command line argument key value pairs
     */
    public Map<String, Optional<String>> getOptions() {
        return options;
    }

    /**
     * Decides if a command line option was specified on the command line or
     * not.
     * <p>
     * Should only be called after the method {@code process()} was invoked.
     *
     * @param name the name of the option we are looking for
     * @return {@code true} if the option was specified on the command line, and
     * {@code false} if the option was not specified
     */
    public Boolean optionExists(String name) {
        return getOptions().containsKey(name);
    }

    /**
     * Get a command line option value.
     * <p>
     * Should only be called after the method {@code process()} was invoked.
     *
     * @param name the name of the option we are looking for
     * @return the value of the option
     */
    public Optional<String> option(String name) {
        return getOptions().containsKey(name) ? getOptions().get(name) : Optional.empty();
    }

    /**
     * Get the non option (file) arguments that were gathered by the method
     * {@code process()}.
     *
     * @return the list of the arguments that do not have name in the order as
     * they appeared on the command line
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Parses the command line to discover all the 'option' arguments and all
     * the 'file' arguments.
     * <p>
     * When the argument starts with single dash {@code -} then it is a
     * <pre>
     * -arg value
     * </pre>
     * <p>
     * option argument. The option value is
     * the next command line argument separated by space on the command line
     * from the name of the options.
     * <p>
     * When an argument does not start with dash then this is a 'command' parameter
     * and it gets into the {@code commands} collection.
     * <p>
     * The order of the parameters is
     * not preserved. The order of 'command' arguments is reserved in the array.
     * <p>
     * Commands and options can be shortened to so few characters that still there is
     * only one command or option starts with that string and thus the command or option
     * is not ambiguous.
     *
     * @param args the arguments passed to the main function
     */
    public void process(final String[] args) {
        for (int i = 0; args != null && i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                final var commandAbbreviated = arg.substring(1);
                final var command = findOption(commandAbbreviated);
                if (i + 1 < args.length) {
                    options.put(command, Optional.of(args[i + 1]));
                } else {
                    options.put(command, Optional.empty());
                }
                i++;
            } else {
                this.commands.add(findCommand(arg));
            }
        }
    }

    private String findCommand(String s) {
        return findIt(s, commanda);
    }

    private String findOption(String s) {
        return findIt(s, optiona);
    }

    private String findIt(String s, String[] strings) {
        final var matches = Arrays.stream(strings).filter(command -> command.startsWith(s)).count();
        if (matches > 1) {
            throw new IllegalArgumentException("Command/option " + s + " is ambiguous");
        }
        return Arrays.stream(strings).filter(command -> command.startsWith(s)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Command/option " + s + " does not exist."));
    }
}
