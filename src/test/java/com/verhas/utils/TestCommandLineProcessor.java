package com.verhas.utils;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestCommandLineProcessor {
	private static final String[] commandLine = new String[] { "--arg1=value1",
			"--arg2=value2", "-arg3", "value3", "fileNameParameter",
			"---dashFile", "--arg4", "-arg5" };

	private static final CommandLineProcessor processor = new CommandLineProcessor();

	@BeforeClass
	public static void processCommandLine() {
		processor.process(commandLine);
	}

	@Test
	public void doesNotThrowExceptionWithNoArg() {
		CommandLineProcessor processorEmptyLine = new CommandLineProcessor();
		processorEmptyLine.process(new String[] {});
	}

	@Test
	public void doesNotThrowExceptionWithNullArg() {
		CommandLineProcessor processorNullLine = new CommandLineProcessor();
		processorNullLine.process(null);
	}

	@Test
	public void processesProperlyFormattedOptions() {
		Assert.assertEquals("value1", processor.option("arg1"));
		Assert.assertEquals("value2", processor.option("arg2"));
		Assert.assertEquals("value3", processor.option("arg3"));
	}

	@Test
	public void processesProperlyFormattedFileNameParameters() {
		Assert.assertEquals("fileNameParameter", processor.getFiles().get(0));
	}

	@Test
	public void processesFileNameParametersStartingWithDash() {
		Assert.assertEquals("-dashFile", processor.getFiles().get(1));
	}

	@Test
	public void valuelessOptionsReturnNullValue() {
		Assert.assertNull(processor.option("arg4"));
		Assert.assertNull(processor.option("arg5"));
	}

	@Test
	public void optionsAreReportedAsExisting() {
		for (int i = 1; i < 6; i++) {
			Assert.assertTrue(processor.optionExists("arg" + i));
		}
	}

	@Test
	public void nonExistingOptionsAreReportedAsNonExisting() {
		CommandLineProcessor processor = new CommandLineProcessor();
		Assert.assertFalse(processor.optionExists("arg0"));
	}
}
