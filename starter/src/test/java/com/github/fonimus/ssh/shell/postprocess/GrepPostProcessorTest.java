package com.github.fonimus.ssh.shell.postprocess;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.fonimus.ssh.shell.postprocess.provided.GrepPostProcessor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GrepPostProcessorTest {

	public static final String TEST = "test\ntoto\ntiti\ntest";

	private static GrepPostProcessor processor;

	@BeforeAll
	static void init() {
		processor = new GrepPostProcessor();
	}

	@Test
	void process() {
		assertAll("grep",
				() -> assertEquals(TEST, processor.process(TEST, null)),
				() -> assertEquals(TEST, processor.process(TEST, Collections.singletonList(""))),
				() -> assertEquals("1. test\n4. test", processor.process(TEST, Collections.singletonList("test"))),
				() -> assertEquals("1. test\n4. test", processor.process(TEST, Arrays.asList("test", "toto"))),
				() -> assertEquals("2. toto", processor.process(TEST, Collections.singletonList("toto")))
		);
	}
}