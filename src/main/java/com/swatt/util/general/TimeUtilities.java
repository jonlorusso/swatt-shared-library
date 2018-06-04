package com.swatt.util.general;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtilities {
	public static final long SECOND = 1000;
	public static final long MINUTE = 60*SECOND;
	public static final long HOUR = 60*MINUTE;
	public static final long DAY = 24*HOUR;
	public static final long WEEK = 7 * DAY;
	
	public static final long HALF_SECOND = 500;
	public static final long TW0_SECONDS = 2*SECOND;
	public static final long FIVE_SECONDS = 5*SECOND;
	public static final long TEN_SECONDS = 10*SECOND;
	public static final long FIFTEEN_SECONDS = 15*SECOND;
	public static final long HALF_MINUTE = 30*SECOND;
	
	public static final long TW0_MINUTES = 2*MINUTE;
	public static final long FIVE_MINUTES = 5*MINUTE;
	public static final long TEN_MINUTES = 10*MINUTE;
	public static final long FIFTEEN_MINUTES = 15*MINUTE;
	public static final long HALF_HOUR = 30*MINUTE;
	
	public static final long SECONDS_IN_MINUTE = 60;
	public static final long SECONDS_IN_HOUR = 60 * SECONDS_IN_MINUTE;
	public static final long SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;
	public static final long SECONDS_IN_TWELVE_HOURS = 12 * SECONDS_IN_HOUR;
	public static final long SECONDS_IN_WEEK = 7 * SECONDS_IN_DAY;
	
	public static final long MINUTES_IN_HOUR = 60;
	public static final long MINUTES_IN_DAY = 24 * MINUTES_IN_HOUR;
	public static final long MINUTES_IN_TWELVE_HOURS = 12 * MINUTES_IN_HOUR;
	public static final long MINUTES_IN_WEEK = 7 * MINUTES_IN_DAY;
	
	public static long adjustAfterTimeforRollover(long afterTime, long beforeTime) {
		while (afterTime < beforeTime)
			afterTime += DAY;
		
		return afterTime;
	}	
	
	public static long[] cullBefore(long times[], long cullTime) {
		int cullPos = 0;
		
		for(int i=0; i < times.length; i++) {
			if(times[i] < cullTime)
				cullPos++;
			else
				break;
		}
		
		if (cullPos == 0)
			return times;
		
		long temp[] = new long[times.length - cullPos];
		System.arraycopy(times,cullPos,temp, 0, temp.length);
		
		return temp;
	}
	
	private static DateFormat niceDate = new SimpleDateFormat("MM/dd/yy hh:mm aa");

	public synchronized static final String asString(long lDate) {
		return niceDate.format(new Date(lDate));
	}
	
	public synchronized static final long asLong(String date) {
		try {
			return niceDate.parse(date).getTime();
		} catch (Throwable t) {
			throw new RuntimeException("Unable to parse date: " + date, t);
		}
	}
					   
}