package polydopter.shareall.common;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	private static PrintStream out;
	private static PrintStream err;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static {
		out = System.out;
		err = System.err;
	}
	
	public static void setOut(PrintStream out) {
		Logger.out = out;
	}
	
	public static void setErr(PrintStream err) {
		Logger.err = err;
	}
	
	public static void info(String message) {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String className = element.getClassName().substring(element.getClassName().lastIndexOf(".") + 1);
		out.printf("[%s][%s][%s-%s] %s\n", sdf.format(new Date()), "INFO", className, Thread.currentThread().getName(), message);
	}
	
	public static void error(String message) {
		error(message, null);
	}
	
	public static void error(Throwable e) {
		error(e.getMessage(), e);
	}

	public static void error(String message, Throwable e) {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		String className = element.getClassName().substring(element.getClassName().lastIndexOf("."));
		err.printf("[%s][%s][%s-%s] %s\n", sdf.format(new Date()), "ERROR", className, Thread.currentThread().getName(), message);
		if(e != null) {
			e.printStackTrace(err);	
		}
	}
}
