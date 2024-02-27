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
import space.sadfox.owlook.logger.LoggerDAO;
import space.sadfox.owlook.logger.LoggerEntity;
import space.sadfox.owlook.logger.LoggerEntry;
import space.sadfox.owlook.ui.tools.MessageBox;

public class Owlook implements Thread.UncaughtExceptionHandler {

  private static LoggerDAO logger;

  static {
    Path pathToLog = Path.of("log.xml");
    JAXBEntityFactory<LoggerEntity> factory = new JAXBEntityFactory<>(LoggerEntity.class);

    try {
      LoggerEntity loggerEntity = factory.instanceOf(pathToLog);
      logger = new LoggerDAO(loggerEntity);
    } catch (JAXBException | IOException | ReflectiveOperationException e) {
      e.printStackTrace();
      logger = new LoggerDAO(new LoggerEntity());
    }
  }

  public synchronized static void registerException(int criticalLevel, Throwable e) {
    LoggerEntry entry = logger.addNewLoggerEntry();

    criticalLevel = normalizeCriticalLevel(criticalLevel);

    entry.setName(e.getClass().getName());
    entry.setMessage(e.getMessage());
    entry.setTime(System.currentTimeMillis());
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    entry.setStackTrace(writer.toString());
    entry.setLogLevel(LogLevel.ERROR);
    entry.setCriticalLevel(criticalLevel);
    try {
      logger.getLoggerEntity().save();
    } catch (JAXBException | IOException e1) {
      e1.printStackTrace();
    }

    printLogEntry(entry);

    if (getConfig().isDebugMode()) {
      e.printStackTrace();
      MessageBox messageBox = new MessageBox(AlertType.ERROR);
      messageBox.setTitle(entry.getName());
      messageBox.setHeaderText(entry.getName());
      messageBox.setContentText(entry.getMassage());
      messageBox.initModality(Modality.NONE);
      messageBox.show();
    }
  }

  public synchronized static void registerMessage(LogMessage message) {
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

    printLogEntry(entry);
  }

  @Override
  public synchronized void uncaughtException(Thread t, Throwable e) {
    registerException(0, e);
    // System.err.print("Exception in thread \"" + t.getName() + "\" ");
    // e.printStackTrace();
  }

  public static OwlookConfiguration getConfig() {
    Path confPath = Path.of("owlook.conf");
    try {
      return new ConfigurationManager<>(OwlookConfiguration.class).getConfig(confPath);
    } catch (JAXBException | IOException | ClassCastException | ReflectiveOperationException e) {
      Owlook.registerException(0, e);
      return null;
    }
  }

  private static int normalizeCriticalLevel(int criticalLevel) {
    if (criticalLevel < 0) {
      return 0;
    } else if (criticalLevel > 2) {
      return 2;
    } else {
      return criticalLevel;
    }
  }

  private static synchronized void printLogEntry(LoggerEntry loggerEntry) {
    String message = "[" + loggerEntry.getLogLevel() + "] " + loggerEntry.getName() + ": "
        + loggerEntry.getMassage();

    if (loggerEntry.getLogLevel().equals(LogLevel.ERROR)) {
      System.err.println(message);
    } else {
      System.out.println(message);
    }
  }
}
