package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlEntityInitializeException;
import space.sadfox.owlook.base.owl.OwlInfo;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public class OwlReference<T extends OwlEntity> extends OwlReferenceBase<T>
    implements Property<Owl<T>>, WritableObjectValue<Owl<T>> {

  public OwlReference(Class<T> targetOwlEntity) {
    super(targetOwlEntity);
  }

  OwlReference(Class<T> targetOwlEntity, List<OwlInfo> owlInfoList, boolean removeUnloadOwlIds) {
    super(targetOwlEntity, owlInfoList, removeUnloadOwlIds);
  }

  private ObjectProperty<Owl<T>> refOwl;


  private ObjectProperty<Owl<T>> objectProperty() {
    // Ленивая иницализация для того чтобы зависимости подтягивались после полной загрузки сов
    if (refOwl == null) {
      refOwl = new SimpleObjectProperty<>();
      OwlLoader l = OwlLoader.INSTANCE;
      if (owlInfoList.size() > 0) {
        try {
          refOwl.set(l.loadOwl(owlInfoList.get(0).id(), targetOwlEntity));
        } catch (OwlCastException | OwlEntityInitializeException | JAXBException | IOException e) {
          Owlook.registerException(e);
        } catch (OwlNotFoundException e) {
          OwlInfo oInfo = owlInfoList.get(0);
          Owlook.notificate(new OwlookMessage(MessageLevel.WARNING, "Missing dependency",
              oInfo.owlName() + " (" + oInfo.id() + ")" + " [" + oInfo.createdModule() + "]"));
        }
      }
      refOwl.addListener((property, oldValue, newValue) -> {
        if (newValue == null) {
          owlInfoList.clear();
        } else {
          owlInfoList.add(0, newValue.info());
        }
      });

      l.addDeleteOwlListener(deleteOwl -> {
        if (deleteOwl.equals(refOwl.get())) {
          refOwl.set(null);
        }
      });
    }
    return refOwl;
  }

  public boolean isEmpty() {
    return objectProperty().get() == null;
  }

  public boolean isPresent() {
    return objectProperty().get() != null;
  }

  public void clear() {
    objectProperty().set(null);
  }

  @Override
  public Owl<T> get() {
    return objectProperty().get();
  };

  @Override
  public void set(Owl<T> value) {
    objectProperty().set(value);
  };

  @Override
  List<Owl<? extends OwlEntity>> owls() {
    return Arrays.asList(get());
  }

  @Override
  public boolean isBound() {
    return objectProperty().isBound();
  };

  @Override
  public void removeListener(ChangeListener<? super Owl<T>> listener) {
    objectProperty().removeListener(listener);
  };

  @Override
  public void removeListener(InvalidationListener listener) {
    objectProperty().removeListener(listener);
  };

  @Override
  public void addListener(ChangeListener<? super Owl<T>> listener) {
    objectProperty().addListener(listener);
  };

  @Override
  public void addListener(InvalidationListener listener) {
    objectProperty().addListener(listener);
  };

  @Override
  public Object getBean() {
    return objectProperty().getBean();
  };

  @Override
  public void setValue(Owl<T> value) {
    objectProperty().setValue(value);
  };

  @Override
  public void bind(ObservableValue<? extends Owl<T>> observable) {
    objectProperty().bind(observable);
  };

  @Override
  public void unbind() {
    objectProperty().unbind();
  };

  @Override
  public void bindBidirectional(Property<Owl<T>> other) {
    objectProperty().bindBidirectional(other);
  };

  @Override
  public void unbindBidirectional(Property<Owl<T>> other) {
    objectProperty().unbindBidirectional(other);
  };

  @Override
  public String getName() {
    return objectProperty().getName();
  };

  @Override
  public Owl<T> getValue() {
    return objectProperty().getValue();
  };
}
