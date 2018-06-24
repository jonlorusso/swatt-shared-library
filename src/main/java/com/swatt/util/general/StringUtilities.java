package com.swatt.util.general;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.swatt.util.io.IoUtilities;

public class StringUtilities {
	public static final String EMPTY_STRING_ARRAY[] = new String[0];
	
	private static final String ESCAPE_TAB = "\\t";
	private static final String TAB = "\t";
	private static final String ESCAPE_NEWLINE = "\\n";
	private static final String NEWLINE = "\n";
	private static final String ESCAPE_BACKSLASH = "\\\\";
	private static final String BACKSLASH = "\\";


	public static boolean isNullOrAllWhiteSpace(String text) {
		if (text == null)
			return true;
		else
			return (text.trim().length() == 0);
	}
	
	public static String getWhitespacePrefix(String text) {
		int i = 0;
		for (; i < text.length(); i++)
			if (!Character.isWhitespace(text.charAt(i)) || (text.charAt(i)+"").equals(NEWLINE))
				break;
		return text.substring(0, i);
	}

	public static boolean isAllWhiteSpace(StringBuffer text) {
		int len = text.length();

		for (int i=0; i < len; i++) {
			if (!Character.isWhitespace(text.charAt(i)))
				return false;
		}

		return true;
	}


	public static boolean isAllWhiteSpace(String text) {
		int len = text.length();

		for (int i=0; i < len; i++) {
			if (!Character.isWhitespace(text.charAt(i)))
				return false;
		}

		return true;
	}
		
	public static String removeFromEnd(String text, int numberOfChars) {
		if (numberOfChars > text.length()) return "";
		return text.substring(0, text.length() - numberOfChars);
	}

	public static String capFirst(String text) {
		char firstChar = text.charAt(0);
		return Character.toUpperCase(firstChar) + text.substring(1);
	}

	public static String lowerFirst(String text) {
		char firstChar = text.charAt(0);
		return Character.toLowerCase(firstChar) + text.substring(1);
	}	

	public static boolean isValidJavaIdentifier(String variable) {
		if (variable == null || variable.length() == 0) return false;
		
		if (!Character.isJavaIdentifierStart(variable.charAt(0)))
			return false;

		for (int i = 1; i < variable.length(); i++)
			if (!Character.isJavaIdentifierPart(variable.charAt(i)))
				return false;

		return true;
	}

	public static boolean endsIn(String text, String endText) {
		if (endText.length() > text.length()) return false;
		else return (text.indexOf(endText, text.length() - endText.length()) != -1);
	}

	public static String escapedStringToUnescapedString(String originalString) {
		String nonEsc = "";
		for (int i=0; i<originalString.length(); i++) {
			char c = originalString.charAt(i);
			if (c == '\t') {
				nonEsc += ESCAPE_TAB;
			} else if (c == '\n') {
				nonEsc += ESCAPE_NEWLINE;
			} else if (c == '\\') {
				nonEsc += ESCAPE_BACKSLASH;
			} else {
				nonEsc += String.valueOf(c);
			}
		}
		return nonEsc;
	}

	public static String escapedStringToUnescapedStringWhitespaceOnly(String originalString) {
		String nonEsc = "";
		for (int i=0; i<originalString.length(); i++) {
			char c = originalString.charAt(i);
			if (c == '\t') {
				nonEsc += ESCAPE_TAB;
			} else if (c == '\n') {
				nonEsc += ESCAPE_NEWLINE;
			} else {
				nonEsc += String.valueOf(c);
			}
		}
		return nonEsc;
	}
	
	public static String unescapedStringToEscapedString(String originalString) {
		String escapedString = "";
		int i = 0;

		for (; i + 1 < originalString.length(); i++) {
		
			if (originalString.substring(i,i+2).equals(ESCAPE_TAB)) {
				escapedString += TAB;
				i++;
			} else if (originalString.substring(i,i+2).equals(ESCAPE_NEWLINE)) {
				escapedString += NEWLINE;
				i++;
			} else if (originalString.substring(i,i+2).equals(ESCAPE_BACKSLASH)) {
				escapedString += BACKSLASH;
				i++;
			} else {
				escapedString += originalString.charAt(i);
			}
		}
		escapedString += originalString.substring(i,originalString.length());
		
		return escapedString;
	}
	
	public static String removeAll(String text, char[] removeAllChars) {
		return removeAll(text,new String(removeAllChars));
	}
	
	public static String removeAll(String text, String removeAllString) {
		if (text==null) return null;
		
		StringBuffer resultText = new StringBuffer();
		
		char[] textChars=text.toCharArray();
		for (int i=0;i<textChars.length;i++) {
			int index=removeAllString.indexOf(textChars[i]);
			if(index>=0) continue;
			else resultText.append(textChars[i]);
		}
		return resultText.toString();
	}
	
	public static String allowOnly(String text, String allowOnlyString) {
		String resultText="";
		if (text==null) return resultText;
		char[] textChars=text.toCharArray();
		for (int i=0;i<textChars.length;i++) {
			int index=allowOnlyString.indexOf(textChars[i]);
			if(index==-1) continue;
			else resultText+=String.valueOf(textChars[i]);
		}
		return resultText;
	}
	
	public static String allowOnly(String text, char[] allowOnlyChars) {
		return removeAll(text,new String(allowOnlyChars));
	}	
	
	
	
	/**
	* Replaces all occurrences of <code>stringToReplace</code> that appear in
	* <code>originalString</code> with <code>replacementString</code>.
	*
	* Note: Search is done from index 0 to originalString.length() and at each
	* occurrence, the search continues from the (location of the occurence) + stringToReplace.length()
	* not (location of occurence) + 1.  
	*
	* Example:
	*	StringUtilities.replace("foofoofoo","foofoo","bar")
	*		returns  the <code>String</code> "barfoo" (because it continued the search from the end
	*   			of "foofoo"
	*		and NOT "barbar" - which would be correct if the search continued at (location of occurence) + 1
	**/
	
	public static String replace (String originalString, String stringToReplace, String replacementString) {
		if ( (stringToReplace == null) || (stringToReplace.length() == 0))
			throw new IllegalArgumentException("Trying to replace the null or empty String");
			
		
		String  newString = "";
		int lastFoundPosition = 0;
		int newFoundPosition = originalString.indexOf(stringToReplace, lastFoundPosition);

		while (newFoundPosition != -1) {
			newString += originalString.substring(lastFoundPosition, newFoundPosition);
			newString += replacementString;
			lastFoundPosition = newFoundPosition + stringToReplace.length();
			newFoundPosition = originalString.indexOf(stringToReplace, lastFoundPosition);
		}
		
		newString += originalString.substring(lastFoundPosition);
		
		return newString;
	}

	/**
	* Pairwise replaces all occurrences of <code>stringToReplace</code> elements that appear in
	* <code>originalString</code> with the correspondng <code>replacementString</code> element
	*
	**/
	
	public static String replace(String originalString, String stringToReplace[], String replacementString[]) {
		String result = originalString;
		
		for (int i=0; i < stringToReplace.length; i++) {
			result = StringUtilities.replace(result, stringToReplace[i], replacementString[i]);
		}

		return result;
	}	

	public static String replace(String originalString, char originalChar, char replacementChar) {
		StringBuffer sb = new StringBuffer(originalString);
		int len = sb.length();

		for (int i=0; i < len; i++)
			if (sb.charAt(i) == originalChar)
				sb.setCharAt(i, replacementChar);

		return new String(sb);

	}

	private static char hexDigits[] = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	
	public static String bytesToHex(byte buf[]) { 
		return bytesToHex(buf, 0, buf.length);
	}

	public static String bytesToHex(byte buf[], int offset, int len) {
		StringBuffer sBuf = new StringBuffer();
		int bytesPerRow = 16;
		int dashAfter = 8;
		
		int byteInRow = 0;
		
		for (int i=0; i < len; i++) {
			byte b = buf[offset + i];

			int hi = (b >> 4) & 0x0F;
			int lo = b & 0x0F;
			
			sBuf.append(hexDigits[hi]);
			sBuf.append(hexDigits[lo]);

			byteInRow++;

			if (byteInRow == dashAfter)
				sBuf.append(" - ");
			else if (byteInRow != bytesPerRow)
				sBuf.append(' ');
			else
				sBuf.append('\n');
			
			byteInRow %= bytesPerRow;
		}

		return sBuf.toString();
	}
	
	public static byte[] hexToBytes(String text) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		for(StringTokenizer st = new StringTokenizer(text, " \t\n\r-"); st.hasMoreTokens(); ) {
			String s = st.nextToken();
			int b = Integer.parseInt(s, 16);
			bout.write(b & 0xFF);
		}
		
		IoUtilities.close(bout);
		
		return bout.toByteArray();
	}
	
	public static String commaSeparate(int elements[]) {
		if (elements == null) 
			return "[null collection]";
		
		StringBuffer result = new StringBuffer();
			
		for (int i=0; i < elements.length; i++) {
			if (i > 0)
				result.append(',');
			
			result.append(elements[i]);
		}
		
		return result.toString();
	}
	
	public static String commaSeparate(Object elements[]) {
		if (elements == null) 
			return "[null collection]";
		
		StringBuffer result = new StringBuffer();
			
		for (int i=0; i < elements.length; i++) {
			if (i > 0)
				result.append(',');
			
			result.append(elements[i]);
		}
		
		return result.toString();
	}

	public static String commaSeparate(Collection<?> elements) {
		if (elements == null) 
			return "[null collection]";
		
		return commaSeparate(elements.iterator());
	}

	
	public static String commaSeparate(Iterator<?> elements) {
		StringBuffer result = new StringBuffer();
		boolean first = true;
		
		for (Iterator<?> i = elements; i.hasNext();) {
			Object element = (Object) i.next();

			if (first)
				first = false;
			else
				result.append(',');
			
			result.append(element);
		}
		
		return result.toString();
	}

	
	
	public static String frontPad(String text, int length) {
		int padSize = length - text.length();
		
		if (padSize > 0) {
			StringBuffer sb = new StringBuffer(length);
			
			for (int i=0; i < padSize; i++)
				sb.append(' ');
			
			sb.append(text);
			
			return sb.toString();
		} else 
			return text;
	}
	
	public static String getByteAsHex(int val) { return getByteAsHex((byte) (val & 0xFF)); }

	
	public static String getByteAsHex(byte val) {
		StringBuffer sb = new StringBuffer(2);
		
		sb.append(hexDigits[val & 0xF]);
		sb.append(hexDigits[(val >> 4) & 0xF]);
		
		return sb.toString();
	}
	
	public static String getByteAsHex(int val, int byteNum) {
		
		switch (byteNum) {
			case 0:
				val = val & 0xFF;
				break;

			case 1:
				val = (val >> 8) & 0xFF;
				break;

			case 2:
				val = (val >> 16) & 0xFF;
				break;

			case 3:
				val = (val >> 24) & 0xFF;
				break;
		}

		return getByteAsHex((byte) val); 
	}
	
	public static int parseInt(String text) {
		if ((text.length() > 2) && (text.charAt(0) == '0') && ((text.charAt(1) == 'x') || text.charAt(1) == 'X')) 
			return Integer.parseInt(text.substring(2), 16);
		else
			return parseInt(text.getBytes());
	}
	
	public static int parseInt(byte buf[]) { return parseInt(buf, 0, buf.length); }

	public static int parseInt(byte buf[], int pos, int len) {
		int result = 0;
		boolean negative = false;
		
		if (buf[pos] == '-') {
			negative = true;
			pos++;
			len--;
		}
		
		int finalPos = pos + len;
		
		for (int i=pos; i < finalPos; i++) {
			int val = buf[i] - '0';
						
			if ((val < 0) || (val > 9))
				throw new NumberFormatException("Unable to Parse as a Number: \"" + new String(buf) + "\"");
			
			result = result*10 + val;
		}
		
		if (negative)
			return -result;
		else
			return result;
	}
	
	public static long parseLong(String s) {	return parseLong(s.getBytes()); }
	public static long parseLong(byte buf[]) { return parseLong(buf, 0, buf.length); }

	public static long parseLong(byte buf[], int pos, int len) {
		long result = 0;
		boolean negative = false;
		
		if (buf[pos] == '-') {
			negative = true;
			pos++;
			len--;
		}
		
		int finalPos = pos + len;
		
		for (int i=pos; i < finalPos; i++) {
			int val = buf[i] - '0';
						
			if ((val < 0) || (val > 9))
				throw new NumberFormatException("Unable to Parse as a Number: \"" + new String(buf) + "\"");
			
			result = result*10 + val;
		}
		
		if (negative)
			return -result;
		else
			return result;
	}
	
	
	
	public static void getBytes(String text, int srcFrom, int srcTo, byte dest[], int destBegin) {
		for (int i=srcFrom; i < srcTo; i++)
			dest[destBegin++] = (byte) text.charAt(i);
	}
	
	public static String removeNonNumberCharacters(String text) {
		byte buf[] = text.getBytes();
		int numDigits = 0;
		
		for (int i=0; i < buf.length; i++) {
			byte b = buf[i];
			
			if ( (b < '0') || (b > '9'))
				continue;
			
			buf[numDigits++] = buf[i];
		}
		
		return new String(buf, 0, numDigits);
	}
	
	public static String asBits(int val, int bitsToShow) {
		char buf[] = new char[bitsToShow];
		
		int bitSbPos = bitsToShow-1;
		
		for (int i=0; i < bitsToShow; i++) {
			buf[bitSbPos] =  ((val & 1) == 1) ? '1' : '0';
			
			val >>= 1;
			bitSbPos--;
		}
		
		return new String(buf);
	}
	
	public static boolean getBooleanValue(String value) { 
		if (value == null)
			return false;
		else
			return value.equalsIgnoreCase("true");
	}
	
	public static Iterator<String> getLines(String text) {
		ArrayList<String> results = new ArrayList<String>();
		
		try {
			text = removeAll(text, "\r");
			StringReader in = new StringReader(text);
			
			BufferedReader bin = new BufferedReader(in);
			
			for(;;) {
					String line = bin.readLine();
					if (line == null)
						
						break;
					
					results.add(line);
			}
		} catch (IOException e) { } // will never occur
		
		return results.iterator();
	}
	
//	public final static boolean hasOpt(String tag, String args[]) {
//		return (CollectionsUtilities.indexOf(args, tag) != -1);
//	}
//	
//	public final static String getOpt(String tag, String args[]) {
//		return getOpt(tag, args, null);
//	}
//
//	
//	public final static String getOpt(String tag, String args[], String defaultValue) {
//		tag = '-'+ tag;
//		int index = CollectionsUtilities.indexOf(args, tag);
//		
//		index++;
//		
//		if ((index != 0) && (index < args.length))
//			return args[index];
//		else 
//			return defaultValue;
//	}
//	
//	public final static boolean getBooleanOpt(String tag, String args[], boolean defaultValue) {
//		String defaultString = defaultValue ? "true" : "false";
//		
//		return getOpt(tag, args, defaultString).toLowerCase().equals("true");
//	}
//
//	public final static int getIntOpt(String tag, String args[], int defaultValue) {
//		return Integer.parseInt(getOpt(tag, args, String.valueOf(defaultValue)));
//	}
//
//	public final static String[] getDelimitedOpts(String tag, String args[], String delimiter) {
//		String text = getOpt(tag, args, "");
//		
//		return makeStringArray(text, delimiter);
//	}
//	
//	public final static String[] getCommaDelimitedOpts(String tag, String args[]) {
//		return getDelimitedOpts(tag, args, ",");
//	}
//	
//	public final static String[] getColonDelimitedOpts(String tag, String args[]) {
//		return getDelimitedOpts(tag, args, ":");
//	}
//	
//	public final static String[] getSemiColonDelimitedOpts(String tag, String args[]) {
//		return getDelimitedOpts(tag, args, ";");
//	}
	
	public final static String[] makeStringArray(String line) {
		ArrayList<String> strings = new ArrayList<String>();
		
		for(StringTokenizer st = new StringTokenizer(line); st.hasMoreElements(); ) {
			strings.add(st.nextToken());
		}
		
		return (String[]) strings.toArray(new String[strings.size()]);
	}


	public final static String[] makeStringArray(String line, String delimiter) {
		ArrayList<String> strings = new ArrayList<String>();
		
		for(StringTokenizer st = new StringTokenizer(line, delimiter); st.hasMoreElements(); ) {
			strings.add(st.nextToken());
		}
		
		return (String[]) strings.toArray(new String[strings.size()]);
	}
	
	public final static String makeNativeOsLines(String text) {
		if (SystemUtilities.isWindowsFamily())
			return makeDosLines(text);
		else
			return makeUnixLines(text);
	}
	
	public final static String makeUnixLines(String text) {
		return text.replaceAll("\r", "");
	}
	
	public final static String makeDosLines(String text) {
		text = makeUnixLines(text);
		return text.replaceAll("\n", "\r\n");
	}
}