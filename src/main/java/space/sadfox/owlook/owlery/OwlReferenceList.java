package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlEntityInitializeException;
import space.sadfox.owlook.base.owl.OwlInfo;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public class OwlReferenceList<T extends OwlEntity> extends OwlReferenceBase<T>
    implements ObservableList<Owl<T>> {

  public OwlReferenceList(Class<T> targetOwlEntity) {
    super(targetOwlEntity);
  }


  OwlReferenceList(Class<T> targetOwlEntity, List<OwlInfo> owlInfoList,
      boolean removeUnloadOwlIds) {
    super(targetOwlEntity, owlInfoList, removeUnloadOwlIds);
  }

  private ObservableList<Owl<T>> refOwls;


  @Override
  List<Owl<? extends OwlEntity>> owls() {
    return new ArrayList<>(this);
  }

  private ObservableList<Owl<T>> owlsProperty() {
    // Ленивая иницализация для того чтобы зависимости подтягивались после полной загрузки сов
    if (refOwls == null) {
      OwlLoader l = OwlLoader.INSTANCE;
      refOwls = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
      List<OwlInfo> removeOwls = null;
      for (OwlInfo owlInfo : owlInfoList) {
        try {
          Owl<T> owl = l.loadOwl(owlInfo.id(), targetOwlEntity);
          refOwls.add(owl);
        } catch (OwlCastException | OwlEntityInitializeException | JAXBException | IOException e) {
          Owlook.registerException(e);
        } catch (OwlNotFoundException e) {
          if (removeUnloadOwlIds) {
            if (removeOwls == null) {
              removeOwls = new ArrayList<>();
            }
            removeOwls.add(owlInfo);
            Owlook.registerMessage(
                new OwlookMessage(MessageLevel.INFO, "OwlReferenceList Load - remove",
                    "Remove owl id since it is not found: " + owlInfo.owlName() + " ("
                        + owlInfo.id() + ")" + " [" + owlInfo.createdModule() + "]"));
          } else {
            Owlook.notificate(
                new OwlookMessage(MessageLevel.WARNING, "Missing dependency", owlInfo.owlName()
                    + " (" + owlInfo.id() + ")" + " [" + owlInfo.createdModule() + "]"));
          }
        }
      }
      if (removeOwls != null) {
        owlInfoList.removeAll(removeOwls);
      }

      refOwls.addListener((ListChangeListener<Owl<T>>) change -> {
        while (change.next()) {
          if (change.wasAdded()) {
            owlInfoList.addAll(change.getFrom(), change.getAddedSubList().stream()
                .map(addedOwl -> addedOwl.info()).collect(Collectors.toList()));
          } else if (change.wasRemoved()) {
            change.getRemoved().forEach(removedOwl -> owlInfoList.remove(removedOwl.info()));
          }
          // TODO: Возможно тут нужно сделать синхронизацию перестановки
          // else if (change.wasPermutated()) {
          //
          // }
        }
      });

      l.addDeleteOwlListener(deleteOwl -> refOwls.remove(deleteOwl));
    }
    return refOwls;
  }

  @Override
  public int size() {
    return owlsProperty().size();
  }

  @Override
  public boolean isEmpty() {
    return owlsProperty().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return owlsProperty().contains(o);
  }

  @Override
  public Iterator<Owl<T>> iterator() {
    return owlsProperty().iterator();
  }

  @Override
  public Object[] toArray() {
    return owlsProperty().toArray();
  }

  @Override
  public <E> E[] toArray(E[] a) {
    return owlsProperty().toArray(a);
  }

  @Override
  public boolean add(Owl<T> e) {
    return owlsProperty().add(e);
  }

  @Override
  public boolean remove(Object o) {
    return owlsProperty().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return owlsProperty().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends Owl<T>> c) {
    return owlsProperty().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends Owl<T>> c) {
    return owlsProperty().addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return owlsProperty().removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return owlsProperty().retainAll(c);
  }

  @Override
  public void clear() {
    owlsProperty().clear();
  }

  @Override
  public Owl<T> get(int index) {
    return owlsProperty().get(index);
  }

  @Override
  public Owl<T> set(int index, Owl<T> element) {
    return owlsProperty().set(index, element);
  }

  @Override
  public void add(int index, Owl<T> element) {
    owlsProperty().add(index, element);
  }

  @Override
  public Owl<T> remove(int index) {
    return owlsProperty().remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return owlsProperty().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return owlsProperty().lastIndexOf(o);
  }

  @Override
  public ListIterator<Owl<T>> listIterator() {
    return owlsProperty().listIterator();
  }

  @Override
  public ListIterator<Owl<T>> listIterator(int index) {
    return owlsProperty().listIterator(index);
  }

  @Override
  public List<Owl<T>> subList(int fromIndex, int toIndex) {
    return owlsProperty().subList(fromIndex, toIndex);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean retainAll(Owl<T>... elements) {
    return owlsProperty().retainAll(elements);
  };

  @SuppressWarnings("unchecked")
  @Override
  public boolean removeAll(Owl<T>... elements) {
    return owlsProperty().removeAll(elements);
  };

  @Override
  public void removeListener(ListChangeListener<? super Owl<T>> listener) {
    owlsProperty().removeListener(listener);
  };

  @Override
  public void removeListener(InvalidationListener listener) {
    owlsProperty().removeListener(listener);
  };

  @SuppressWarnings("unchecked")
  @Override
  public boolean addAll(Owl<T>... elements) {
    return owlsProperty().addAll(elements);
  };

  @Override
  public void addListener(ListChangeListener<? super Owl<T>> listener) {
    owlsProperty().addListener(listener);
  };

  @Override
  public void addListener(InvalidationListener listener) {
    owlsProperty().addListener(listener);
  };

  @Override
  public void remove(int from, int to) {
    owlsProperty().remove(from, to);
  };

  @Override
  public boolean setAll(Collection<? extends Owl<T>> col) {
    return owlsProperty().setAll(col);
  };

  @SuppressWarnings("unchecked")
  @Override
  public boolean setAll(Owl<T>... elements) {
    return owlsProperty().setAll(elements);
  };
}
