package com.javax0.license3j.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestCommandLineProcessor {
	private static final String[] commandLine = new String[] { "--arg1=value1",
			"--arg2=value2", "-arg3", "value3", "fileNameParameter",
			"---dashFile", "--arg4", "-arg5" };

	private static final CommandLineProcessor processor = new CommandLineProcessor(commands, optiona);

	@BeforeAll
	public static void processCommandLine() {
		processor.process(commandLine);
	}

	@Test
	public void doesNotThrowExceptionWithNoArg() {
		CommandLineProcessor processorEmptyLine = new CommandLineProcessor(commands, optiona);
		processorEmptyLine.process(new String[] {});
	}

	@Test
	public void doesNotThrowExceptionWithNullArg() {
		CommandLineProcessor processorNullLine = new CommandLineProcessor(commands, optiona);
		processorNullLine.process(null);
	}

	@Test
	public void processesProperlyFormattedOptions() {
		Assertions.assertEquals("value1", processor.option("arg1"));
		Assertions.assertEquals("value2", processor.option("arg2"));
		Assertions.assertEquals("value3", processor.option("arg3"));
	}

	@Test
	public void processesProperlyFormattedFileNameParameters() {
		Assertions.assertEquals("fileNameParameter", processor.getCommands().get(0));
	}

	@Test
	public void processesFileNameParametersStartingWithDash() {
		Assertions.assertEquals("-dashFile", processor.getCommands().get(1));
	}

	@Test
	public void valuelessOptionsReturnNullValue() {
		Assertions.assertNull(processor.option("arg4"));
		Assertions.assertNull(processor.option("arg5"));
	}

	@Test
	public void optionsAreReportedAsExisting() {
		for (int i = 1; i < 6; i++) {
			Assertions.assertTrue(processor.optionExists("arg" + i));
		}
	}

	@Test
	public void nonExistingOptionsAreReportedAsNonExisting() {
		CommandLineProcessor processor = new CommandLineProcessor(commands, optiona);
		Assertions.assertFalse(processor.optionExists("arg0"));
	}
}
