package com.swatt.util.general;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class SystemUtilities {
	
	public static String getEnv(String name) {
		return System.getenv().get(name);
	}
	
	public static String getEnv(String name, String defaultVal) {
		String val =  System.getenv().get(name);
		return (val == null) ? defaultVal : val;
	}
	
	public static Properties mergeEnv(Properties properties) {
		return mergeEnv(properties, "");
	}
	
	public static Properties mergeEnv(Properties properties, String prefix) {
		Map<String, String> envMap = System.getenv();
		
		for (String key : envMap.keySet()) {
			String envKey = prefix + key;
			String value = envMap.get(envKey);
			if (value != null)
				properties.put(key, value);
		}
		
		return properties;
	}
	
	public static Properties loadAndMergeEnv(String propertiesFileName) throws IOException {
		return loadAndMergeEnv(propertiesFileName);
	}

	
	public static Properties loadAndMergeEnv(String propertiesFileName, String prefix) throws IOException {
		Properties properties = CollectionsUtilities.loadProperties(propertiesFileName);
		return mergeEnv(properties, prefix);
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
