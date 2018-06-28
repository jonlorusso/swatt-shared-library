//package com.swatt.util.general;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//public class StringUtilitiesTest {
package com.swatt.util.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.swatt.util.io.ResourceUtilities;

public class StringUtilitiesTest {

	@Test
	public void testNativeLineSeparators() {
		
		boolean isWindows = SystemUtilities.isWindowsFamily();
		
		if (isWindows) {
			String s = "This\nis a\nTest" ;
		
			String w = StringUtilities.makeNativeOsLines(s);  // convert from Unix windows/Dos
			String x = StringUtilities.makeUnixLines(w);
			String y = StringUtilities.makeDosLines(x);
			String z = StringUtilities.makeUnixLines(y);
			
			assertEquals(s, x);
			assertEquals(x, z);
			assertEquals(w, y);
			
			assertEquals(s.length(), y.length() - 2);
			
		} else {
			String s = "This\r\nis a\r\nTest" ;
			
			String w = StringUtilities.makeNativeOsLines(s);  // convert from windows/Dos to Unix
			String x = StringUtilities.makeDosLines(w);
			String y = StringUtilities.makeUnixLines(x);
			String z = StringUtilities.makeDosLines(y);
			
			assertEquals(s, x);
			assertEquals(x, z);
			assertEquals(w, y);
			
			assertEquals(s.length(), y.length() + 2);
		}

	}
	
	@Test
	public void testIsNullOrAllWhiteSpace() {
		String emptyString = "";
		String nonNullString = "abc";
		String blankString = "   \t\t    \t";
		
		assertTrue(StringUtilities.isAllWhiteSpace(emptyString));
		assertFalse(StringUtilities.isAllWhiteSpace(nonNullString));
		assertTrue(StringUtilities.isAllWhiteSpace(blankString));
	}

}
