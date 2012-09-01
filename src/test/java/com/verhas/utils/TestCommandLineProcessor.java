package com.verhas.utils;

import junit.framework.Assert;

import org.junit.Test;

public class TestCommandLineProcessor {
	@Test
	public void testnoArgs() {
		CommandLineProcessor processor = new CommandLineProcessor();
		processor.process(new String[] {});
		processor.process(null);
	}

	@Test
	public void testArgs1() {
		CommandLineProcessor processor = new CommandLineProcessor();
		processor
				.process(new String[] { "--arg1=value1", "--arg2=value2",
						"-arg3", "value3", "nooption", "---dashFile", "--arg4",
						"-arg5" });
		Assert.assertEquals("value1", processor.option("arg1"));
		Assert.assertEquals("value2", processor.option("arg2"));
		Assert.assertEquals("value3", processor.option("arg3"));
		Assert.assertEquals("nooption", processor.getFiles().get(0));
		Assert.assertEquals("-dashFile", processor.getFiles().get(1));
		Assert.assertNull(processor.option("arg4"));
		Assert.assertNull(processor.option("arg5"));
		for (int i = 1; i < 6; i++) {
			Assert.assertTrue(processor.optionExists("arg" + i));
		}
		Assert.assertFalse(processor.optionExists("arg0"));
	}
}
