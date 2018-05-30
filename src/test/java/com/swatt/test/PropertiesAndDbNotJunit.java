package com.swatt.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.swatt.util.general.SystemUtilities;
import com.swatt.util.sql.ConnectionPool;
import com.swatt.util.sql.SqlUtilities;

public class PropertiesAndDbNotJunit {
	
    public static void main(String[] args) {
		try {
			String configFile = "data/conf/PropertiesAndDbTest.conf";
			String prefix = "";
			
			if (args.length > 0) 
				configFile = args[0];
			
			if (args.length > 1)
				prefix = args[1];
			
			
			Properties configProperties = SystemUtilities.loadAndMergeEnv(configFile, prefix);
			
			String jdbcUrl = SqlUtilities.getJdbcURL(configProperties);
			System.out.println("jdbcUrl: " + jdbcUrl);
			
			ConnectionPool pool = new ConnectionPool(configProperties);
			
			Connection conn = pool.getConnection();
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM EXCHANGES");
			
			while (rs.next()) {
				String name = rs.getString(2);
				System.out.println("name: " + name);
			}
			
			conn.close();
			
		}  catch(Throwable t)  {
			t.printStackTrace();
		}
	}

}
