package com.javax0.license3j;

import java.io.*;

public class Repl {

    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static void say(String format, Object... params) {
        System.out.println(String.format(format, params));
    }

    public static void main(String[] args) {
        say("License3j REPL");
        say("CDW is %s", new File(".").getAbsolutePath());
        final LocalConsole console;
        if (System.console() == null) {
            say("[WARN] No console in the system");
            console = new BufferedReaderConsole();
        } else {
            console = new ConsoleConsole();
        }
        final var app = new CommandLineApp();
        for (; ; ) {
            final var line = console.readLine("L3j> $ ");
            if (line == null) {
                return;
            }
            final var lineT = line.trim();
            if (lineT.equalsIgnoreCase("")) {
                continue;
            }
            if (lineT.trim().equals("exit")) {
                return;
            }
            if (lineT.startsWith("!")) {
                shell(lineT.substring(1));
                continue;
            }
            try {
                app.execute(lineT);
            } catch (Exception e) {
                say("[EXCEPTION] " + e);
                say("[INFO] %s", app.usage());
            }
            final var errors = app.getErrors();
            if (!errors.isEmpty()) {
                for (String error : errors) {
                    say("[ERROR] " + error);
                }
            }
            final var messages = app.getMessages();
            if (!messages.isEmpty()) {
                for (String message : messages) {
                    say("[INFO] " + message);
                }
            }
        }
    }

    private static void shell(String s) {
        try {
            if (s.startsWith("cd ")) {
                say("[ERROR] you can not change the working directory");
                return;
            }
            final Process process;
            if (isWindows) {
                process = Runtime.getRuntime()
                        .exec(String.format("cmd.exe /c %s", s));
            } else {
                process = Runtime.getRuntime()
                        .exec(String.format("sh -c %s", s));
            }
            final var sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            say("[SHELL OUTPUT]\n%s[END SHELL OUTPUT]", sb.toString());
        } catch (IOException e) {
            say("[EXCEPTION] " + e);
        }
    }

    private interface LocalConsole {
        String readLine(String msg);
    }

    private static class ConsoleConsole implements LocalConsole {
        Console console = System.console();

        public String readLine(String msg) {
            return console.readLine(msg);
        }
    }

    private static class BufferedReaderConsole implements LocalConsole {
        private final BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));

        public String readLine(String msg) {
            try {
                System.out.print(msg);
                return reader.readLine();
            } catch (IOException e) {
                say("Cannot read from standard input...\nNo more fallback");
                e.printStackTrace(System.err);
            }
            return null;
        }
    }
}
