package space.sadfox.owlook.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerDAO {

	private static final String DATE_FORMAT = "HH:mm:ss";

	private Logger logger;

	public LoggerDAO(Logger logger) {
		this.logger = logger;
	}

	public LoggerEntry addNewLoggerEntry() {
		LoggerEntry entry = new LoggerEntry();
		logger.getLogs().add(entry);
		return entry;
	}

	public String getTimeFormatString(LoggerEntry entry) {
		return new SimpleDateFormat(DATE_FORMAT).format(new Date(entry.getTime()));
	}
	
	

}
