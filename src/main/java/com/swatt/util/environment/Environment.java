package com.swatt.util.environment;

import java.util.Properties;

public class Environment {
    public static String getEnvironmentVariableValueOrDefault(String environmentVariableName, String propertyName, Properties properties) {
        String value = System.getenv().get(environmentVariableName);
        if (value == null) {
            value = properties.getProperty(propertyName);
        }
        return value;
    }
}
