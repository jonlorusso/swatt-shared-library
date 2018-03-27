package com.swatt.util.general;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ExceptionUtilities {
	public static String toString(Throwable throwable) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream(bout);
		throwable.printStackTrace(pout);
		pout.close();
		return bout.toString();
	}
	
	public static String toString(Throwable throwable, boolean showFullStackTrace) {
		if (showFullStackTrace)
			return toString(throwable);
		else {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream pout = new PrintStream(bout);
	
			for (boolean first = true;throwable != null; throwable = throwable.getCause()) {
				pout.println( (first ? "" : "Caused By: ") + throwable.toString());
				first = false;
			}
			
			pout.close();
			return bout.toString();
		}
	}
	
	public static boolean isCausedBy(Throwable t, Class<?> throwableClass) {
		for(; t != null; t = t.getCause()) {
			if (throwableClass.isInstance(t))
				return true;
		}
		
		return false;
	}
	
	public static void printCauses(Throwable t) { printCauses(t, System.err); }

	
	public static void printCauses(Throwable throwable, PrintStream out) {
		for(Throwable t = throwable; t != null; t = t.getCause()) {
			if (t != throwable) 
				out.print("Caused by: ");
			
			out.println(t.getMessage());
		}
		
	}

}
