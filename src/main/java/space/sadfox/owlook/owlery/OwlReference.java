package space.sadfox.owlook.owlery;

import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableObjectValue;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public class OwlReference<T extends OwlEntity> extends OwlReferenceBase<T>
    implements Property<Owl<T>>, WritableObjectValue<Owl<T>> {

  private ObjectProperty<Owl<T>> refOwl;

  public OwlReference(Class<T> targetOwlEntity) {
    super(targetOwlEntity);
  }

  OwlReference(Class<T> targetOwlEntity, State state) {
    super(targetOwlEntity, state);
  }

  private ObjectProperty<Owl<T>> objectProperty() {
    // Ленивая иницализация для того чтобы зависимости подтягивались после полной загрузки сов
    if (refOwl == null) {
      refOwl = new SimpleObjectProperty<>();
      OwlLoader l = OwlLoader.INSTANCE;
      if (state.owlIds.size() > 0) {
        try {
          refOwl.set(l.getOwl(state.owlIds.get(0), targetOwlEntity));
        } catch (OwlCastException e) {
          Owlook.registerException(e);
        } catch (OwlNotFoundException e) {
          Owlook.notificate(new OwlookMessage(MessageLevel.WARNING, "Missing dependency",
              state.owlIds.get(0).toString()));
        }
      }
      refOwl.addListener((property, oldValue, newValue) -> {
        if (newValue == null) {
          state.owlIds.clear();
        } else {
          state.owlIds.set(0, newValue.info().id());
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
