package space.sadfox.owlook.utils;

import space.sadfox.owlook.logger.LogLevel;

public class LoggerMessage {
	private String name;
	private String message;
	private LogLevel logLevel;

	public LoggerMessage(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public LoggerMessage(LogLevel logLevel, String name) {
		this.logLevel = logLevel;
		this.name = name;
	}

	public LoggerMessage(LogLevel logLevel, String name, String message) {
		this.logLevel = logLevel;
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}
	
	

}
