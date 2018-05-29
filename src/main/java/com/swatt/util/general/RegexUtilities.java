package com.swatt.util.general;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtilities {
	public static String multiLineFind(String text, String regex) {
		return multiLineFind(text, regex, 0);
	}
	
	public static String multiLineFind(String text, String regex, int group) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		
		Matcher matcher = pattern.matcher(text);
		
		matcher.find();
		return matcher.group(group);
	}
	
	public static ArrayList<String> findAll(String text, String regex) {
		return findAll(text, regex, 0);	
	}
	
	public static ArrayList<String> findAll(String text, String regex, int group) {
		Pattern pattern = Pattern.compile(regex);
		
		return findAll(text, pattern, group);
	}
	
	public static ArrayList<String> findAll(String text, Pattern pattern, int group) {
        ArrayList<String> result = new ArrayList<String>();
        
		Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
        	String s = matcher.group(group);
        	result.add(s);
        }
        
        return result;
	}
}