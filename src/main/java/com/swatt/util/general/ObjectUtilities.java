package com.swatt.util.general;

public class ObjectUtilities {
	
	public static final boolean isBothNull(Object obj1, Object obj2) {
		return (obj1 == null) && (obj2 == null);
	}
	
	
	public static final boolean isBothNonNull(Object obj1, Object obj2) {
		return (obj1 != null) && (obj2 != null);
	}

	public static final boolean equals(Object obj1, Object obj2) {
		if (obj1 == null) {
			return (obj2 == null);
		} else {
			if (obj2 != null)
				return obj1.equals(obj2);
			else 
				return false;
		}
	}

	public static final boolean toBoolean(Object obj) {
		if (obj instanceof Boolean) {
			Boolean b = (Boolean) obj;
			return b.booleanValue();
		} else if (obj instanceof String) {
			String s = (String) obj;
			return s.equalsIgnoreCase("true");
		} else
			throw new IllegalArgumentException("Not convertable to a boolean");
	}
		
	public static final int toInt(Object obj) {
		if (obj instanceof Number) {
			Number number = (Integer) obj;
			return number.intValue();
		} else if (obj instanceof String) {
			String s = (String) obj;
			return Integer.parseInt(s);
		} else
			throw new IllegalArgumentException("Not convertable to a int");
	}
		
	public static final long toLong(Object obj) {
		if (obj instanceof Number) {
			Number number = (Integer) obj;
			return number.longValue();
		} else if (obj instanceof String) {
			String s = (String) obj;
			return Long.parseLong(s);
		} else
			throw new IllegalArgumentException("Not convertable to a long");
	}
		
	public static final double toDouble(Object obj) {
		if (obj instanceof Number) {
			Number number = (Integer) obj;
			return number.doubleValue();
		} else if (obj instanceof String) {
			String s = (String) obj;
			return Double.parseDouble(s);
		} else
			throw new IllegalArgumentException("Not convertable to a double");
	}

}
