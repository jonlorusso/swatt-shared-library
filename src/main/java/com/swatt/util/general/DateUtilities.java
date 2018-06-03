package com.swatt.util.general;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class DateUtilities {
	
	public static Date adjustDate(Date date, long adjust) {
		long time = date.getTime() + adjust;
		return new Date(time);
	}
	
	public static String convert(String sDate, DateFormat fromFormat, DateFormat toFormat) throws ParseException {
		Date date = fromFormat.parse(sDate);
		return toFormat.format(date);
	}
}
