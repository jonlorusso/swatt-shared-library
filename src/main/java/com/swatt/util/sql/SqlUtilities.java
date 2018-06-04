package com.swatt.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import com.swatt.util.general.OperationFailedException;

public class SqlUtilities {
    public static final String DATABASE_TYPE_KEY = "DATABASE_TYPE";
    public static final String DATABASE_HOST_KEY = "DATABASE_HOST";
    public static final String DATABASE_PORT_KEY = "DATABASE_PORT";
    public static final String DATABASE_NAME_KEY = "DATABASE_NAME";
    public static final String DATABASE_USER_KEY = "DATABASE_USER";
    public static final String DATABASE_PASSWORD_KEY = "DATABASE_PASSWORD";

    
    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String JDBC_ODBC_DRIVER_CLASS_NAME = "sun.jdbc.odbc.JdbcOdbcDriver";
//    public static final String ORACLE_DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver";
//    public static final String SQL_SERVER_DRIVER_CLASS_NAME = "com.jnetdirect.jsql.JSQLDriver";
    public static final String SQL_LITE_DRIVER_CLASS_NAME = "org.sqlite.JDBC";

    private static final String allDrivers[] = { JDBC_ODBC_DRIVER_CLASS_NAME, MYSQL_DRIVER_CLASS_NAME, SQL_LITE_DRIVER_CLASS_NAME };
    private static HashMap<String, String> driverMap = new HashMap<String, String>();
    
    static {
    	driverMap.put("mysql", MYSQL_DRIVER_CLASS_NAME);
    	driverMap.put("odbc", JDBC_ODBC_DRIVER_CLASS_NAME);
    	driverMap.put("sqlite", SQL_LITE_DRIVER_CLASS_NAME);
    }

    public static void loadJdbcOdbcDriver() throws OperationFailedException {
        loadByClass(JDBC_ODBC_DRIVER_CLASS_NAME);
    }

//    public static void loadJdbcOracleDriver() throws OperationFailedException {
//        loadByClass(ORACLE_DRIVER_CLASS_NAME);
//    }

    public static void loadJdbcOMySqlDriver() throws OperationFailedException {
        loadByClass(MYSQL_DRIVER_CLASS_NAME);
    }

    public static void loadStandardDrivers() {
        for (int i = 0; i < allDrivers.length; i++) {
            try {
                loadByClass(allDrivers[i]); // load the ones that are available
            } catch (OperationFailedException e) {
            }
        }
    }

//    public static void loadJdbcSqlServerDriver() throws OperationFailedException {
//        loadByClass(SQL_SERVER_DRIVER_CLASS_NAME);
//    }
//
    
    public static void loadMySqlDriver() throws OperationFailedException {
        loadByClass(MYSQL_DRIVER_CLASS_NAME);
    }
    
    private static void loadByClass(String className) throws OperationFailedException {
    	try {
    		Class.forName(className);
    	} catch (ClassNotFoundException e) {
    		throw new OperationFailedException("Unable to load driver for: " + className);
    	}
    	
    }

    public static void loadJdbcSqlLiteDriver() throws OperationFailedException {
        loadByClass(SQL_LITE_DRIVER_CLASS_NAME);
    }
    
    public static void loadJdbcDriver(String dbType) throws OperationFailedException {
    	dbType = dbType.toLowerCase();
    	String className = driverMap.get(dbType);
    	
    	if (className != null)
    		loadByClass(className);
    	else 
    		throw new OperationFailedException("no JDBC driver found for: " + dbType);
    }
    
    public static void loadJdbcDriver(Properties properties) throws OperationFailedException {
    	String databaseType = properties.getProperty(DATABASE_TYPE_KEY);
    	
    	try {
	    	loadJdbcDriver(databaseType);
    	} catch (OperationFailedException e) {
    		throw new OperationFailedException("JDBC driver not found for: " + databaseType, e);
    	}
    }
    
    public static String createJdbcUrl(String databaseType, String host, String port, String databaseName) throws OperationFailedException {
    	loadJdbcDriver(databaseType);
	    	
    	if ((port != null) && (port.length() != 0))
    		return String.format("jdbc:%s://%s:%s/%s", databaseType, host, port, databaseName);
    	else {
    		return String.format("jdbc:%s://%s/%s", databaseType, host, databaseName);
    	}
    }
    
    public static String createJdbcUrl(String databaseType, String host, String databaseName) throws OperationFailedException {
    	return createJdbcUrl(databaseType, host, null, databaseName);
    }
    
    public static String getJdbcURL(Properties properties) throws OperationFailedException {
    	String databaseType = properties.getProperty(DATABASE_TYPE_KEY);
    	String host = properties.getProperty(DATABASE_HOST_KEY);
    	String port = properties.getProperty(DATABASE_PORT_KEY);
    	String databaseName = properties.getProperty(DATABASE_NAME_KEY);
    	
    	return createJdbcUrl(databaseType, host, port, databaseName);
    }
    
    public static String getUser(Properties properties) {
    	return properties.getProperty(DATABASE_USER_KEY);
    }
    
    public static String getPassword(Properties properties) {
    	return properties.getProperty(DATABASE_PASSWORD_KEY);
    }

    public static Connection getConnection(String jdbcUrl) throws SQLException {
        return getConnection(jdbcUrl, null, null);
    }
    
    public static Connection getConnection(String jdbcUrl, String user, String password) throws SQLException {
        try {
            if (user == null)
                return DriverManager.getConnection(jdbcUrl);
            else
                return DriverManager.getConnection(jdbcUrl, user, password);

        } catch (SQLException e) {
            throw e;
        }
    }

    public static Connection getConnection(Properties properties) throws OperationFailedException, SQLException {
    	String jdbcUrl = getJdbcURL(properties);
    	String user = getUser(properties);
    	String password = getPassword(properties);
    	
    	if (user == null) 
    		return getConnection(jdbcUrl);
    	else
    		return getConnection(jdbcUrl, user, password);
    }

    public static int emptyTable(Connection conn, String tableName) throws SQLException {
        Statement statement = conn.createStatement();
        int result = emptyTable(statement, tableName);
        statement.close();
        return result;
    }

    public static int emptyTable(Statement statement, String tableName) throws SQLException {
        String sql = getEmptyTableSql(tableName);
        return statement.executeUpdate(sql);
    }

    public static String getEmptyTableSql(String tableName) {
        return "TRUNCATE TABLE " + tableName;
    }
    
    public static void loadCsv(Connection connection, String filePath, String tableName, int linesIgnore, String columnList) throws SQLException {
        String loadQuery = "LOAD DATA LOCAL INFILE '" + filePath + "' INTO TABLE " + tableName + " FIELDS TERMINATED BY ','" + " LINES TERMINATED BY '\n' IGNORE " + linesIgnore + " LINES (" + columnList + ") ";
        Statement stmt = connection.createStatement();
        stmt.execute(loadQuery);
    }

    public static void close(Connection connection) {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
        }
    }

    public static void close(Statement statement) {
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException e) {
        }
    }
}
