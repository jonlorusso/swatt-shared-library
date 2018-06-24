package com.swatt.util.general;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swatt.util.io.ResourceUtilities;

public class ResourceUtiliitesTest {
	private static final String RESOURCE_FILE_CONTENTS = "This is a test Resource File";
	private static final String RESOURCE_FILE_NAME = "testResourceFile.txt";

	@Test
	public void testResourceTextFromClass() throws IOException {
		String t = ResourceUtilities.getResourceAsText(ResourceUtiliitesTest.class, RESOURCE_FILE_NAME);
		
		assertEquals(t, RESOURCE_FILE_CONTENTS);
	}
	
	@Test
	public void testResourceTextFromPath() throws IOException {
		String path = "/com/swatt/util/general/" + RESOURCE_FILE_NAME;
		
		String t = ResourceUtilities.getResourceAsText(path);
		
		assertEquals(t, RESOURCE_FILE_CONTENTS);
	}
	

}
