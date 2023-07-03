package space.sadfox.owlook.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.components.logger.LogLevel;
import space.sadfox.owlook.components.logger.Logger;
import space.sadfox.owlook.components.logger.LoggerDAO;
import space.sadfox.owlook.components.logger.LoggerEntry;
import space.sadfox.owlook.jaxb.EntityLoader;

public class ErrorLogger implements Thread.UncaughtExceptionHandler {

	public static void registerException(Throwable e) {
		Logger logger = getErrorLogger();
		LoggerDAO loggerDAO = new LoggerDAO(logger);
		LoggerEntry entry = loggerDAO.addNewLoggerEntry();
		entry.setName(e.getClass().getName());
		entry.setMessage(e.getMessage());
		entry.setTime(System.currentTimeMillis());
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		entry.setStackTrace(writer.toString());
		entry.setLogLevel(LogLevel.ERROR);

		e.printStackTrace();
	}
	
	public static void registerMessage(LoggerMessage message) {
		Logger logger = getErrorLogger();
		LoggerDAO loggerDAO = new LoggerDAO(logger);
		LoggerEntry entry = loggerDAO.addNewLoggerEntry();
		entry.setName(message.getName());
		entry.setMessage(message.getMessage());
		entry.setLogLevel(message.getLogLevel());
		entry.setTime(System.currentTimeMillis());
		System.out.println(message.getLogLevel());
		System.out.println(message.getName());
		System.out.println(message.getMessage());
	}

	public static void registerException(Thread t, Throwable e) {
		registerException(e);
		System.err.print("Exception in thread \"" + t.getName() + "\" ");
		e.printStackTrace();
	}

	public static Logger getErrorLogger() {
		Path path = ProjectPath.LOG.getPath()
				.resolve(new SimpleDateFormat("dd-MM-yyyy").format(new Date(System.currentTimeMillis())));
		try {
			return EntityLoader.INSTANCE.createOrLoadExternalEntity(path, Logger.class);
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		registerException(t, e);
	}
}
