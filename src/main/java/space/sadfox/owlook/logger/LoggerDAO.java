package space.sadfox.owlook.logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerDAO {

	private static final String DATE_FORMAT = "HH:mm:ss";

	private LoggerEntity loggerEntity;

	public LoggerDAO(LoggerEntity loggerEntity) {
		this.loggerEntity = loggerEntity;
	}

	public LoggerEntry addNewLoggerEntry() {
		LoggerEntry entry = new LoggerEntry();
		loggerEntity.getLogs().add(entry);
		return entry;
	}

	public String getTimeFormatString(LoggerEntry entry) {
		return new SimpleDateFormat(DATE_FORMAT).format(new Date(entry.getTime()));
	}
	
	

}
