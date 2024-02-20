package space.sadfox.owlook.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import jakarta.xml.bind.JAXBException;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import space.sadfox.owlook.OwlookConfiguration;
import space.sadfox.owlook.base.jaxb.JAXBEntityFactory;
import space.sadfox.owlook.logger.LogLevel;
import space.sadfox.owlook.logger.LoggerDAO;
import space.sadfox.owlook.logger.LoggerEntity;
import space.sadfox.owlook.logger.LoggerEntry;
import space.sadfox.owlook.ui.tools.MessageBox;

public class Logger implements Thread.UncaughtExceptionHandler {

  private static LoggerDAO logger;

  static {
    Path pathToLog = ProjectPath.LOG.getPath().resolve("log.xml");
    JAXBEntityFactory<LoggerEntity> factory = new JAXBEntityFactory<>(LoggerEntity.class);

    try {
      LoggerEntity loggerEntity = factory.instanceOf(pathToLog);
      logger = new LoggerDAO(loggerEntity);
    } catch (JAXBException | IOException | ReflectiveOperationException e) {
      e.printStackTrace();
      logger = new LoggerDAO(new LoggerEntity());
    }
  }

  public synchronized static void registerException(int loggingDepth, Throwable e) {
    LoggerEntry entry = logger.addNewLoggerEntry();

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
      logger.getLoggerEntity().save();
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
        messageBox.setContentText(entry.getStackTrace().substring(0, 1000) + "...");
        messageBox.initModality(Modality.NONE);
        messageBox.show();
      }
    }
  }

  public synchronized static void registerMessage(LoggerMessage message) {
    LoggerEntry entry = logger.addNewLoggerEntry();
    entry.setName(message.getName());
    entry.setMessage(message.getMessage());
    entry.setLogLevel(message.getLogLevel());
    entry.setTime(System.currentTimeMillis());
    try {
      logger.getLoggerEntity().save();
    } catch (JAXBException | IOException e) {
      e.printStackTrace();
    }
    System.out.println(
        "[" + message.getLogLevel() + "] " + message.getName() + ": " + message.getMessage());
  }

  public synchronized static void registerException(Thread t, Throwable e) {
    registerException(0, e);
    System.err.print("Exception in thread \"" + t.getName() + "\" ");
    e.printStackTrace();
  }

  @Override
  public synchronized void uncaughtException(Thread t, Throwable e) {
    registerException(t, e);
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
}
