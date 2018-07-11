package com.swatt.util.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.swatt.util.general.OperationFailedException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {
    public static final String DATABASE_CONNECTION_POOL_MAX_SIZE_KEY = "DATABASE_CONNECTION_POOL_MAX_SIZE";

    private String jdbcUrl;
    private String user;
    private String password;

    private HikariDataSource dataSource;
    
    static {
        try {
            SqlUtilities.loadMySqlDriver();
        } catch (OperationFailedException e) {
            e.printStackTrace();
        }
    }

    public ConnectionPool(Properties properties) throws OperationFailedException {
        String sMaxSize = null;

        try {
            this.jdbcUrl = SqlUtilities.getJdbcURL(properties);
            this.user = SqlUtilities.getUser(properties);
            password = SqlUtilities.getPassword(properties);
            sMaxSize = properties.getProperty(DATABASE_CONNECTION_POOL_MAX_SIZE_KEY);

            int maxSize = 5;

            if (sMaxSize != null)
                maxSize = Integer.parseInt(sMaxSize);

            init(jdbcUrl, user, password, maxSize);

        } catch (NumberFormatException t) {
            throw new OperationFailedException("Invalid Connection Pool Size: " + sMaxSize);
        }
    }

    public ConnectionPool(String jdbcUrl, String user, String password, int maxSize) {
        init(jdbcUrl, user, password, maxSize);

    }

    private void init(String jdbcUrl, String user, String password, int maxSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(maxSize);
        
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}