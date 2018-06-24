package com.swatt.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;


public class ResourceUtilities {
	public static URL getResource(Class<?> clazz, String resourceName) {
		return clazz.getResource(resourceName);
	}
	
	public static URL getResource(Object obj, String resourceName) {
		if (obj instanceof Class) 
			return getResource((Class<?>) obj, resourceName);
		else
			return getResource(obj.getClass(), resourceName);
	}
	
	public static InputStream getResourceAsStream(Class<?> clazz, String resourceName) {
		return clazz.getResourceAsStream(resourceName);
	}
	
	public static InputStream getResourceAsStream(Object obj, String resourceName) {
		if (obj == null) {
			return getResourceAsStream(Object.class, resourceName); 
		} if (obj instanceof Class) 
			return getResourceAsStream((Class<?>) obj, resourceName);
		else
			return getResourceAsStream(obj.getClass(), resourceName);
	}
	
	public static String getResourceAsText(String resourceName) throws IOException {
		if (resourceName.startsWith("/"))
			return getResourceAsText(Object.class, resourceName);
		else
			throw new IOException("Resources not bound to a class must be fully qualified (ie start with '/')");
	}

	
	public static String getResourceAsText(Class<?> clazz, String resourceName) throws IOException {
		InputStream in = clazz.getResourceAsStream(resourceName);
		return IoUtilities.streamToString(in);
	}
	
	public static String getResourceAsText(Object obj, String resourceName) throws IOException {
		if (obj == null) {
			return getResourceAsText(Object.class, resourceName); 
		} if (obj instanceof Class) 
			return getResourceAsText((Class<?>) obj, resourceName);
		else
			return getResourceAsText(obj.getClass(), resourceName);
	}
	
	public static ResourceBundle getResourceBundle(String bundleName) throws IOException {
		return getResourceBundle(bundleName, null);
	}
	
	public static ResourceBundle getResourceBundle(String bundleName, Locale locale) throws IOException{
		if (locale == null)
			return ResourceBundle.getBundle(bundleName);
		else
			return ResourceBundle.getBundle(bundleName, locale);
	}
	
	public static ResourceBundle getResourceBundle(Class<?> clazz, String bundleName) {
		return getResourceBundle(clazz, bundleName, null);
	}
	
	public static ResourceBundle getResourceBundle(Class<?> clazz, String bundleName, Locale locale) {
		String fullName = null;
	
		fullName = clazz.getPackage().getName() + '.' + bundleName;

		if (locale == null)
			return ResourceBundle.getBundle(fullName);
		else
			return ResourceBundle.getBundle(fullName, locale);
	}
	
	public static String getResourceableText(String text, ResourceBundle resourceBundle, String resourcePrefix) {
		String result = null;
		if (resourceBundle == null)
			result = text;
		else if (resourcePrefix == null)
			result = resourceBundle.getString(text);
		else 
			result = resourceBundle.getString(resourcePrefix + text);
		
		if (result == null)
			return text;
		else
			return result;
	}
}
