package com.swatt.util.general;

import java.util.Properties;

public class SystemUtilities {
	
	public static String getEnv(String name) {
		return System.getenv().get(name);
	}
	
	public static String getEnv(String name, String defaultVal) {
		String val =  System.getenv().get(name);
		return (val == null) ? defaultVal : val;
	}
	
    public static String getEnvOrProperty(String name, Properties properties) {
    	return getEnvOrProperty(name, properties, null);
    }

	
    public static String getEnvOrProperty(String name, Properties properties, String defaultVal) {
        String value = properties.getProperty(name);
        if (value == null) {
            value = System.getenv().get(name);
            
            if (value == null)
            	value = defaultVal;
        }
        return value;
    }
    
    public static String getPropertyOrEnv(String name, Properties properties) {
    	return getEnvOrProperty(name, properties, null);
    }
}
