package space.sadfox.owlook.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.xml.bind.JAXBException;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import space.sadfox.owlook.OwlookConfiguration;
import space.sadfox.owlook.logger.LogLevel;
import space.sadfox.owlook.logger.Logger;
import space.sadfox.owlook.logger.LoggerDAO;
import space.sadfox.owlook.logger.LoggerEntry;
import space.sadfox.owlook.ui.tools.MessageBox;

public class OwlLogger implements Thread.UncaughtExceptionHandler {

	public static void registerException(int loggingDepth, Throwable e) {
		Logger logger = getErrorLogger();
		LoggerDAO loggerDAO = new LoggerDAO(logger);
		LoggerEntry entry = loggerDAO.addNewLoggerEntry();

		loggingDepth = normalizeLoggingDepth(loggingDepth);

		entry.setName(e.getClass().getName());
		entry.setMessage(e.getMessage());
		entry.setTime(System.currentTimeMillis());
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		entry.setStackTrace(writer.toString());
		entry.setLogLevel(LogLevel.ERROR);
		entry.setLoggingDepth(loggingDepth);
		try {
			logger.save();
		} catch (JAXBException | IOException e1) {
			e1.printStackTrace();
		}

		OwlookConfiguration config = OwlookConfiguration.instance();

		if (loggingDepth <= config.getLoggingDepth()) {
			e.printStackTrace();
			if (config.isDebugMode()) {
				MessageBox messageBox = new MessageBox(AlertType.ERROR);
				messageBox.setTitle(entry.getName());
				messageBox.setHeaderText(entry.getMassage());
				messageBox.setContentText(entry.getStackTrace());
				messageBox.initModality(Modality.NONE);
				messageBox.show();
			}
		}

		

	}

	private static int normalizeLoggingDepth(int loggingDepth) {
		if (loggingDepth < 0) {
			return 0;
		} else if (loggingDepth > 3) {
			return 3;
		} else {
			return loggingDepth;
		}
	}

	public static void registerMessage(LoggerMessage message) {
		Logger logger = getErrorLogger();
		LoggerDAO loggerDAO = new LoggerDAO(logger);
		LoggerEntry entry = loggerDAO.addNewLoggerEntry();
		entry.setName(message.getName());
		entry.setMessage(message.getMessage());
		entry.setLogLevel(message.getLogLevel());
		entry.setTime(System.currentTimeMillis());
		try {
			logger.save();
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		System.out.println(message.getLogLevel());
		System.out.println(message.getName());
		System.out.println(message.getMessage());
	}

	public static void registerException(Thread t, Throwable e) {
		registerException(0, e);
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
