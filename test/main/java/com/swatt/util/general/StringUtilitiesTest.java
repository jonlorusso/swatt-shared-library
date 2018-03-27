package com.swatt.util.general;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringUtilitiesTest {
	
	@BeforeAll
	static void setupBeforeClass() throws Exception {
//		System.out.println("   IN BEFORE ALL");
	}

	@BeforeEach
	void foo() {
//		System.out.println("Before Each");
	}


	@Test
	void testIsNullOrAllWhiteSpace() {
		String emptyString = "";
		String nonNullString = "abc";
		String blankString = "   \t\t    \t";
		
		assertTrue(StringUtilities.isAllWhiteSpace(emptyString));
		assertFalse(StringUtilities.isAllWhiteSpace(nonNullString));
		assertTrue(StringUtilities.isAllWhiteSpace(blankString));
	}

}
