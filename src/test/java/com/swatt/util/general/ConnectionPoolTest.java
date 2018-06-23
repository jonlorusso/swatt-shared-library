package com.swatt.util.general;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.swatt.util.sql.ConnectionPool;

public class ConnectionPoolTest {
	
	private static ConnectionPool cp;
	private static int nextUser = 100;
	private static long startTestTime = System.currentTimeMillis();
	
	private static class ConnectionUser {
		private int user = nextUser++;
		private long startTime;
		
		ConnectionUser(long idle, long runFor) {
			ConcurrencyUtilities.sleep(50);	// Spread out the thread start times
			
			
			ConcurrencyUtilities.startThread(() -> {
				try {
					display("Started");
					final Connection conn = cp.getConnection();
					startTime =  System.currentTimeMillis();
					final long runUntil = startTime + runFor;
					
					display("Got Connection");
					
					
					for (;;) {
						if (System.currentTimeMillis() < runUntil) {
							Statement s = conn.createStatement();
							ResultSet rs = s.executeQuery("Select * from exchanges limit 1");
							rs.next();
//							String name = rs.getString(2);
//							System.out.println("name: " + name);
							
							s.execute("INSERT INTO GTEST (NAME) VALUES ('Gerry')");
							
							ConcurrencyUtilities.sleep(idle);
						} else
							break;
					}
					
					display("Normal Connection Close");

					conn.close();
					
				} catch (Throwable t) { 
					display("Exception " + t.getMessage());
				}
				
				display("End of Thread, runtime: "  + (System.currentTimeMillis() - startTime));
			}, "UserThread-" + user);
		}
		
		private void display(String message) {
			System.out.println("User " + user + " (" + (System.currentTimeMillis() - startTestTime) + "): " + message);
		}
		
	}
	
	public static void main(String[] args) {
		try {
			String jdbcUrl = "jdbc:mysql://localhost/swatt";
			String user = "test";
			String password = "test";
			int maxPoolSize = 3;
			
			int defaultConnectionLeaseDuration = 3000;
			int maxConnectionIdleTime = 1000;
			int minPoolSize = 1;
			int leaseCheckInterval = 500;
			
			int nextTranchAt = (defaultConnectionLeaseDuration * 3) / 4;
			
			int totalTestTime = 15 * 1000;
			
			cp = new ConnectionPool(jdbcUrl, user, password, maxPoolSize);
			
			
			cp.setDefaultConnectionLeaseDuration(defaultConnectionLeaseDuration);
			cp.setMaxConnectionIdleTime(maxConnectionIdleTime);
			cp.setMinPoolSize(minPoolSize);
			cp.setLeaseCheckInterval(leaseCheckInterval);
			
			for (int i=0; i < 7; i++) {
				new ConnectionUser(100, i*1000);
			}
			
			ConcurrencyUtilities.sleep(nextTranchAt);
			
			for (int i=0; i < 7; i++) {
				new ConnectionUser(100, i*1000);
			}
			
			ConcurrencyUtilities.sleep(totalTestTime - (System.currentTimeMillis() - startTestTime));
			System.out.println("** CLOSING POOL NOW *** (" + (System.currentTimeMillis() - startTestTime) + ")");
			cp.close(100000);
			
			System.out.println("** POOL CLOSED *** (" + (System.currentTimeMillis() - startTestTime) + ")");

					
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}

}
