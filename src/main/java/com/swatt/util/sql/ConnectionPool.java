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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.swatt.util.general.ConcurrencyUtilities;

/**
 * Connection Pool supporting 
 * <BR><BR>
 * LeaseDuration:   If a 'client' that has gotten the connection holds it longer than this time, then the pool will take it back leaving them an invalid connection.  This is important for when clients getConnection, but don't return connection. <BR>
 *   A LeaseDuration of '0' will allow clients to hold connections forever<BR>
 *   The DefaultLeaseDuration is used with getConnection().  It is overridden on a per connection basis when using getConnection(leaseDuration).<BR>
 *   The default value for DefaultLeaseDuration is ZERO (0), so it should be set if a different value is desired.<BR>
 *   <BR>
 *   MaxPoolSize: Is the maximum number of underlying DB connections that can be doled out by the Connection Pool<BR>
 *   <BR>
 *   MinPoolSize + MaxConnectionIdleTime are used to close idle connections.   Any returned connection that is still in the free pool for MaxConnectionIdleTime will be removed.<BR>
 *   BUT to avoid slow restart times, if MinPoolSize is set connections will remain in the free pool to respect that minimum limit<BR>
 *
 **/

public class ConnectionPool {
	private static final long MINUTE = 60 * 1000;
	private static final long HOUR = 60 * MINUTE;

    private String jdbcUrl;
    private String user;
    private String password;
    private int maxPoolSize;
    private int minPoolSize = 0;

	private volatile long defaultConnectionLeaseDuration = 0;
	private long leaseCheckInterval = 1 * MINUTE;			

	private long maxConnectionIdleTime = 1 * HOUR;

    private LinkedList<Entry> freeConnections = new LinkedList<Entry>();
    private LinkedList<Entry> busyConnections = new LinkedList<Entry>();

    private class Entry {
        private Connection connection;
		private long lastCheckoutTime;							// How long has this been in the busyConnection pool. (aka held by client)
		private long lastCheckinTime;							// How long has this been in the freeConnection pool.
		private long leaseDuration;								// Entry specific Lease time;
		
        Entry(Connection conn) {
            this.connection = conn;
        }
        
		void checkoutConnection() {
			this.lastCheckoutTime = System.currentTimeMillis();
		}

		boolean isValid() throws SQLException {

			boolean valid = connection.isValid(1);
			
			if (!valid) 
				SqlUtilities.close(connection);		// Close it just in case.
			
			return valid;			
		}
		
		void checkIn() {
			lastCheckinTime = System.currentTimeMillis();
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
        this.maxPoolSize = maxSize;
        
		ConcurrencyUtilities.startThread(() -> {
			ArrayList<Entry> abandonedEntrys = new ArrayList<Entry>();
			ArrayList<Entry> langishingEntries = new ArrayList<Entry>();
			
			for(;;) {
				ConcurrencyUtilities.sleep(leaseCheckInterval);
				
				// First Check for overdue connections
					
				synchronized(freeConnections) {
					long now = System.currentTimeMillis();
					abandonedEntrys.clear();
					
					for(Entry entry: busyConnections) {			// First find abandoned entrys
						if (entry.leaseDuration > 0) {			// Zero lease means allow Connection to be held forever.
							long inUseFor = now - entry.lastCheckoutTime;
							
							if (inUseFor > entry.leaseDuration) 
								abandonedEntrys.add(entry);
						}
					}
					
					for (Entry entry: abandonedEntrys) {		// Then delete them from the list
						try {
							releaseConnection(entry);			// This removes entry from busy and adds back to free
						} catch (SQLException e) {} 			// will never be thrown
					}
				}
				
				// Now check to see if there are any connections ready for retirement.
				
				synchronized(freeConnections) {
					long now = System.currentTimeMillis();
					langishingEntries.clear();

					for (Entry entry: freeConnections) {
						long idleFor = now - entry.lastCheckinTime;
						
						if (idleFor > maxConnectionIdleTime)
							langishingEntries.add(entry);
					}
					
					int numRemaining = freeConnections.size();
					
					for (Entry entry: langishingEntries) {		// Retire Languishing connections
						
						if ((numRemaining-1) >= minPoolSize) {
							SqlUtilities.close(entry.connection);			// Close the underlying connection first
							freeConnections.remove(entry);
							numRemaining--;
						}
					}
				}
			}
			
		}, "ConnectionPoolLeaseCheck");
    }

    public Connection getConnection() throws SQLException {
    	return getConnection(defaultConnectionLeaseDuration);
    }
    
    public Connection getConnection(long leaseDuration) throws SQLException {
        synchronized (freeConnections) {

            Entry entry = null;

            for (;;) {
                entry = getNextFreeAndValidConnection();

                if (entry != null) {
                    break;
                } else if ((freeConnections.size() + busyConnections.size()) < maxPoolSize) {
                    Connection conn = SqlUtilities.getConnection(jdbcUrl, user, password);
                    entry = new Entry(conn);
                    entry.leaseDuration = leaseDuration;
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

            if (entry.isValid())
                return entry;
        }

        return null;
    }

    public void returnConnection(Connection conn) throws SQLException {
		conn.close();					//  releases ConnecitonHolder if not already released
    }

    private void releaseConnection(Entry entry) throws SQLException {
        synchronized (freeConnections) {
            if (busyConnections.remove(entry)) {
				entry.checkIn();
                freeConnections.add(entry);
                ConcurrencyUtilities.notifyAll(freeConnections);
            } else
                throw new SQLException("Connection did not come from this pool");
        }
    }

	public final long getDefaultConnectionLeaseDuration() { return defaultConnectionLeaseDuration; }
	public final void setDefaultConnectionLeaseDuration(long defaultConnectionLeaseDuration) { this.defaultConnectionLeaseDuration = defaultConnectionLeaseDuration; }
	public final long getMaxConnecitonIdleTime() { return maxConnectionIdleTime; }
	public final void setMaxConnecitonIdleTime(long maxConnecitonIdleTime) { this.maxConnectionIdleTime = maxConnecitonIdleTime; }
	public final int getMinPoolSize() { return minPoolSize; }
	public final void setMinPoolSize(int minPoolSize) { this.minPoolSize = minPoolSize; }

	
    private class ProxyConnection implements Connection {
        private Entry entry;
        private Connection connection;
        private boolean closed = false;

        ProxyConnection(Entry entry) {
            this.entry = entry;
            this.connection = entry.connection;
        	entry.checkoutConnection();
        }

        public void close() throws SQLException {
            if (!closed) {
                releaseConnection(entry);
                
                connection = null;
                
                closed = true;
            }
        }

        public Statement createStatement() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createStatement();
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareCall(sql);
        }

        public String nativeSQL(String sql) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.nativeSQL(sql);
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setAutoCommit(autoCommit);
        }

        public boolean getAutoCommit() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getAutoCommit();
        }

        public void commit() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.commit();
        }

        public void rollback() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.rollback();
        }

        public boolean isClosed() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.isClosed();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getMetaData();
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setReadOnly(readOnly);
        }

        public boolean isReadOnly() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.isReadOnly();
        }

        public void setCatalog(String catalog) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setCatalog(catalog);
        }

        public String getCatalog() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getCatalog();
        }

        public void setTransactionIsolation(int level) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setTransactionIsolation(level);
        }

        public int getTransactionIsolation() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getTransactionIsolation();
        }

        public SQLWarning getWarnings() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.clearWarnings();
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getTypeMap();
        }

        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setTypeMap(map);
        }

        public void setHoldability(int holdability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setHoldability(holdability);
        }

        public int getHoldability() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getHoldability();
        }

        public Savepoint setSavepoint() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.setSavepoint();
        }

        public Savepoint setSavepoint(String name) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.setSavepoint(name);
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.rollback(savepoint);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.releaseSavepoint(savepoint);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql, autoGeneratedKeys);
        }

        public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql, columnIndexes);
        }

        public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.prepareStatement(sql, columnNames);
        }

        public Clob createClob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createClob();
        }

        public Blob createBlob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createBlob();
        }

        public NClob createNClob() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createNClob();
        }

        public SQLXML createSQLXML() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createSQLXML();
        }

        public boolean isValid(int timeout) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.isValid(timeout);
        }

        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            connection.setClientInfo(name, value);
        }

        public void setClientInfo(Properties properties) throws SQLClientInfoException {
            connection.setClientInfo(properties);
        }

        public String getClientInfo(String name) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getClientInfo(name);
        }

        public Properties getClientInfo() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getClientInfo();
        }

        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createArrayOf(typeName, elements);
        }

        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.createStruct(typeName, attributes);
        }

        public void setSchema(String schema) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setSchema(schema);
        }

        public String getSchema() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getSchema();
        }

        public void abort(Executor executor) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.abort(executor);
        }

        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            connection.setNetworkTimeout(executor, milliseconds);
        }

        public int getNetworkTimeout() throws SQLException {
            if (closed)
                throw new SQLException("Connection Closed");

            return connection.getNetworkTimeout();
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