package com.swatt.util.sql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;

public class ConnectionPoolTest {
	
	private ConnectionPool connectionPool;
	
    @Before
    public void setUp() throws Exception {
        SqlUtilities.loadH2Driver();
        connectionPool = new ConnectionPool("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1", "user", "password", 10);
    }

	@Test
	public void testGetConnection() throws Exception {
	    int expected = 99;
	    
	    Connection connection = connectionPool.getConnection();
	    PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT %d", expected));
	    ResultSet resultSet = preparedStatement.executeQuery();
	    
	    if (resultSet.next()) {
	        int actual = resultSet.getInt(1);
	        assertThat(actual, is(equalTo(expected)));
	    } else {
	        fail();
	    }
	}
}
