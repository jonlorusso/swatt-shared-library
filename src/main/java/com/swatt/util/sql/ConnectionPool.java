package com.swatt.util.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.swatt.util.general.ConcurrencyUtilities;

// FIXME: Not Industrial Strength.  Does not deal with un-returned connections
public class ConnectionPool {

    private static final long HOUR = 60 * 60 * 1000;

    private String jdbcUrl;
    private String user;
    private String password;
    private int maxSize;
    private long maxAge = 5 * HOUR;
    private LinkedList<Entry> freeConnections = new LinkedList<>();
    private LinkedList<Entry> busyConnections = new LinkedList<>();

    private class Entry {
        private Connection conn;
        private long started = System.currentTimeMillis();

        Entry(Connection conn) {
            this.conn = conn;
        }

        boolean isValid() throws SQLException {
            long now = System.currentTimeMillis();
            long alive = now - started;

            return (alive < maxAge) && conn.isValid(1);
        }
    }

    static {
        try {
            SqlUtilities.loadMySqlDriver();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ConnectionPool(String jdbcUrl, String user, String password, int maxSize) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        this.maxSize = maxSize;
    }

    public final long getMaxAge() {
        return maxAge;
    }

    public final void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public Connection getConnection() throws SQLException {
        synchronized (freeConnections) {

            Entry entry = null;

            for (;;) {
                entry = getNextFreeAndValidConnection();

                if (entry != null) {
                    break;
                } else if ((freeConnections.size() + busyConnections.size()) < maxSize) {
                    Connection conn = SqlUtilities.getConnection(jdbcUrl, user, password);
                    entry = new Entry(conn);
                    break;
                } else {
                    ConcurrencyUtilities.waitOn(freeConnections);
                }
            }

            busyConnections.add(entry);

            return new ProxyConnection(entry);
        }
    }

    private Entry getNextFreeAndValidConnection() throws SQLException {
        Entry entry = null;

        while (freeConnections.size() > 0) {
            entry = freeConnections.removeFirst();
            Connection conn = entry.conn;

            if (entry.isValid())
                return entry;
        }

        return null;
    }

    public void returnConnection(Connection conn) throws SQLException {
        conn.close(); // releases ConnecitonHolder if not already released
    }

    private void releaseConnection(Entry entry) throws SQLException {
        synchronized (freeConnections) {
            if (busyConnections.remove(entry)) {
                freeConnections.add(entry);
                ConcurrencyUtilities.notifyAll(freeConnections);
            } else
                throw new SQLException("Connection did not come from this pool");
        }
    }

    private class ProxyConnection implements Connection {
        private Entry entry;
        private Connection conn;
        private boolean closed = false;

        ProxyConnection(Entry entry) {
            this.entry = entry;
            this.conn = entry.conn;
        }

        public void close() throws SQLException {
            if (!closed) {
                releaseConnection(entry);

                conn = null;

                closed = true;
            }
        }

        public Statement createStatement() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createStatement();
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareCall(sql);
        }

        public String nativeSQL(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.nativeSQL(sql);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setAutoCommit(autoCommit);
        }

        public boolean getAutoCommit() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getAutoCommit();
        }

        public void commit() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.commit();
        }

        public void rollback() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.rollback();
        }

        public boolean isClosed() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.isClosed();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getMetaData();
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setReadOnly(readOnly);
        }

        public boolean isReadOnly() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.isReadOnly();
        }

        public void setCatalog(String catalog) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setCatalog(catalog);
        }

        public String getCatalog() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getCatalog();
        }

        public void setTransactionIsolation(int level) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setTransactionIsolation(level);
        }

        public int getTransactionIsolation() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getTransactionIsolation();
        }

        public SQLWarning getWarnings() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.clearWarnings();
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createStatement(resultSetType, resultSetConcurrency);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getTypeMap();
        }

        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setTypeMap(map);
        }

        public void setHoldability(int holdability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setHoldability(holdability);
        }

        public int getHoldability() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getHoldability();
        }

        public Savepoint setSavepoint() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.setSavepoint();
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.setSavepoint(name);
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.rollback(savepoint);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.releaseSavepoint(savepoint);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql, autoGeneratedKeys);
        }

        public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql, columnIndexes);
        }

        public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.prepareStatement(sql, columnNames);
        }

        public Clob createClob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createClob();
        }

        public Blob createBlob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createBlob();
        }

        public NClob createNClob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createNClob();
        }

        public SQLXML createSQLXML() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createSQLXML();
        }

        public boolean isValid(int timeout) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.isValid(timeout);
        }

        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            conn.setClientInfo(name, value);
        }

        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            conn.setClientInfo(properties);
        }

        public String getClientInfo(String name) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getClientInfo(name);
        }

        public Properties getClientInfo() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getClientInfo();
        }

        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createArrayOf(typeName, elements);
        }

        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.createStruct(typeName, attributes);
        }

        public void setSchema(String schema) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setSchema(schema);
        }

        public String getSchema() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getSchema();
        }

        public void abort(Executor executor) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.abort(executor);
        }

        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            conn.setNetworkTimeout(executor, milliseconds);
        }

        public int getNetworkTimeout() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return conn.getNetworkTimeout();
        }

        public <T> T unwrap(final Class<T> iface) throws SQLException {
            try {
                if (isWrapperFor(iface)) {
                    return iface.cast(this);
                } else {
                    throw new SQLException("The receiver is not a wrapper and does not implement the interface");
                }
            } catch (Exception e) {
                throw new SQLException("The receiver is not a wrapper and does not implement the interface");
            }
        }

        public boolean isWrapperFor(final Class<?> interfaceOrWrapper) throws SQLException {
            return interfaceOrWrapper.isInstance(this);
        }
    }
}