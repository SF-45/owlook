package space.sadfox.owlook.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.ResourceTarget;
import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;
import space.sadfox.owlook.base.moduleapi.VersionFormat;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "owlookConfiguration")
public class OwlookConfiguration extends ObservedJAXBEntity {
  private VersionFormat version;
  private final BooleanProperty debugMode = new SimpleBooleanProperty(true);
  private final BooleanProperty skipModuleManage = new SimpleBooleanProperty(false);
  private final ObservableList<String> modules = FXCollections.observableArrayList();
  private final ObjectProperty<NotificationPos> notificationPos =
      new SimpleObjectProperty<>(NotificationPos.TOP_RIGHT);
  private final BooleanProperty deleteForgottenOwls = new SimpleBooleanProperty(true);

  public VersionFormat getVersion() {
    return version;
  }

  @XmlElement
  public boolean isDebugMode() {
    return debugModeProperty().get();
  }

  public void setDebugMode(boolean debugMode) {
    debugModeProperty().set(debugMode);
  }

  public BooleanProperty debugModeProperty() {
    return debugMode;
  }

  @XmlElement
  public boolean isSkipModuleManage() {
    return skipModuleManageProperty().get();
  }

  public void setSkipModuleManage(boolean skipModuleManage) {
    skipModuleManageProperty().set(skipModuleManage);
  }

  public BooleanProperty skipModuleManageProperty() {
    return skipModuleManage;
  }

  @XmlElementWrapper(name = "modules")
  @XmlElement(name = "module")
  public List<String> getModules() {
    return modulesProperty();
  }

  public ObservableList<String> modulesProperty() {
    return modules;
  }

  public ObjectProperty<NotificationPos> notificationPosProperty() {
    return notificationPos;
  }

  @XmlElement
  public NotificationPos getNotificationPos() {
    return notificationPos.get();
  }

  public void setNotificationPos(NotificationPos notificationPos) {
    this.notificationPos.set(notificationPos);
  }

  public BooleanProperty deleteForgottenOwlsProperty() {
    return deleteForgottenOwls;
  }

  @XmlElement
  public Boolean isDeleteForgottenOwls() {
    return deleteForgottenOwls.get();
  }

  public void setDeleteForgottenOwls(Boolean deleteForgottenOwls) {
    this.deleteForgottenOwls.set(deleteForgottenOwls);
  }

  @Override
  public List<Object> getProperties() {
    return Arrays.asList(debugMode, modules, skipModuleManage, notificationPos,
        deleteForgottenOwls);
  }

  @Override
  protected void initialization() {
    super.initialization();
    try {
      Properties properties = new Properties();
      properties.load(ResourceTarget.class.getResourceAsStream("pom.properties"));
      version = VersionFormat.of(properties.getProperty("version", "0.0.0-default"));
    } catch (IOException e) {
      Owlook.registerException(0, e);
    }
  }

}
