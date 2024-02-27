package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import space.sadfox.owlook.base.jaxb.ObservedJAXBEntity;
import space.sadfox.owlook.utils.ConfigurationManager;
import space.sadfox.owlook.utils.Owlook;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "owleryConfiguration")
public class OwleryConfig extends ObservedJAXBEntity {
  public static enum SelectedShowBy {
    ALL, OWL_NAME, MODULE;
  }

  private final BooleanProperty idColumnVisible = new SimpleBooleanProperty(true);
  private final BooleanProperty titleColumnVisible = new SimpleBooleanProperty(true);
  private final BooleanProperty owlNameColumnVisible = new SimpleBooleanProperty(false);
  private final BooleanProperty moduleColumnVisible = new SimpleBooleanProperty(false);
  private final BooleanProperty dateColumnVisible = new SimpleBooleanProperty(false);

  private final BooleanProperty editOwlPreview = new SimpleBooleanProperty(true);

  private final ObjectProperty<SelectedShowBy> selectedShowBy =
      new SimpleObjectProperty<>(SelectedShowBy.OWL_NAME);

  private final DoubleProperty groupDividerPos = new SimpleDoubleProperty(0.2d);
  private final DoubleProperty editPreviewDividerPos = new SimpleDoubleProperty(0.7d);

  @XmlElement
  public Boolean getIdColumnVisible() {
    return idColumnVisible.get();
  }

  public void setIdColumnVisible(Boolean idColumnVisible) {
    this.idColumnVisible.set(idColumnVisible);
  }

  public BooleanProperty idColumnVisibleProperty() {
    return idColumnVisible;
  }

  @XmlElement
  public boolean getTitleColumnVisible() {
    return titleColumnVisible.get();
  }

  public void setTitleColumnVisible(boolean titleColumnVisible) {
    this.titleColumnVisible.set(titleColumnVisible);
  }

  public BooleanProperty titleColumnVisibleProperty() {
    return titleColumnVisible;
  }

  @XmlElement
  public boolean getOwlNameColumnVisible() {
    return owlNameColumnVisible.get();
  }

  public void setOwlNameColumnVisible(boolean owlNameColumnVisible) {
    this.owlNameColumnVisible.set(owlNameColumnVisible);
  }

  public BooleanProperty owlNameColumnVisibleProperty() {
    return owlNameColumnVisible;
  }

  @XmlElement
  public boolean getModuleColumnVisible() {
    return moduleColumnVisible.get();
  }

  public void setModuleColumnVisible(boolean moduleColumnVisible) {
    this.moduleColumnVisible.set(moduleColumnVisible);
  }

  public BooleanProperty moduleColumnVisibleProperty() {
    return moduleColumnVisible;
  }

  @XmlElement
  public boolean getDateColumnVisible() {
    return dateColumnVisible.get();
  }

  public void setDateColumnVisible(boolean dateColumnVisible) {
    this.dateColumnVisible.set(dateColumnVisible);
  }

  public BooleanProperty dateColumnVisibleProperty() {
    return dateColumnVisible;
  }

  @XmlElement
  public SelectedShowBy getSelectedShowBy() {
    return selectedShowBy.get();
  }

  public void setSelectedShowBy(SelectedShowBy selectedShowBy) {
    this.selectedShowBy.set(selectedShowBy);
  }

  public ObjectProperty<SelectedShowBy> selectedShowByProperty() {
    return selectedShowBy;
  }

  @XmlElement
  public boolean getEditOwlPreview() {
    return editOwlPreview.get();
  }

  public void setEditOwlPreview(boolean editOwlPreview) {
    this.editOwlPreview.set(editOwlPreview);
  }

  public BooleanProperty editOwlPreviewProperty() {
    return editOwlPreview;
  }

  @XmlElement
  public double getGroupDividerPos() {
    return groupDividerPos.get();
  }

  public void setGroupDividerPos(double groupDividerPos) {
    this.groupDividerPos.set(groupDividerPos);;
  }

  public DoubleProperty groupDividerPosProperty() {
    return groupDividerPos;
  }

  @XmlElement
  public double getEditPreviewDividerPos() {
    return editPreviewDividerPos.get();
  }

  public void setEditPreviewDividerPos(double editPreviewDividerPos) {
    this.editPreviewDividerPos.set(editPreviewDividerPos);
  }

  public DoubleProperty editPreviewDividerPosProperty() {
    return editPreviewDividerPos;
  }

  @Override
  public List<Object> getProperties() {
    return Arrays.asList(idColumnVisible, titleColumnVisible, owlNameColumnVisible,
        moduleColumnVisible, dateColumnVisible, selectedShowBy, editOwlPreview,
        editPreviewDividerPos, groupDividerPos);
  }

  public static OwleryConfig instance() {
    try {
      return new ConfigurationManager<>(OwleryConfig.class).getConfig("owlery");
    } catch (ClassCastException | JAXBException | IOException | ReflectiveOperationException e) {
      Owlook.registerException(0, e);
      return null;
    }
  }

}
