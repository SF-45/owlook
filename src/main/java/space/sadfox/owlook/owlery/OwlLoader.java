package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.xml.bind.JAXBException;
import javafx.scene.control.Alert.AlertType;
import space.sadfox.owlook.base.owl.HollowOwl;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlEntityInitializeException;
import space.sadfox.owlook.base.owl.Owls;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.ui.tools.MessageBox;
import space.sadfox.owlook.utils.Logger;
import space.sadfox.owlook.utils.ProjectPath;
import space.sadfox.owlook.utils.StageFactory;

public enum OwlLoader {
  INSTANCE;

  @FunctionalInterface
  public static interface CreateOwlListener {
    void create(Owl<? extends OwlEntity> entity);
  }

  @FunctionalInterface
  public static interface DeleteOwlListener {
    void delete(Owl<? extends OwlEntity> entity);
  }

  @FunctionalInterface
  public static interface DuplicateOwlListener {
    void duplicate(Owl<? extends OwlEntity> oldEntity, Owl<? extends OwlEntity> newEntity);
  }

  private final Map<UUID, Owl<?>> owls = new HashMap<>();
  private Map<UUID, HollowOwl> ref = new HashMap<>();
  private boolean isBoot = false;

  private final List<CreateOwlListener> createListeners = new ArrayList<>();
  private final List<DeleteOwlListener> deleteListeners = new ArrayList<>();
  private final List<DuplicateOwlListener> duplicateListeners = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public <T extends OwlEntity> Owl<T> getOwl(UUID uuid, Class<T> target)
      throws OwlCastException, OwlNotFoundException {
    if (!owls.containsKey(uuid)) {
      throw new OwlNotFoundException("Owl not found: " + uuid);
    }
    Owl<?> owl = owls.get(uuid);
    if (!owl.entityClass().equals(target)) {
      throw new OwlCastException("Can't to cast an owl to" + target.getSimpleName());
    }
    return (Owl<T>) owl;
  }

  @SuppressWarnings("unchecked")
  public synchronized <T extends OwlEntity> List<Owl<T>> getOwls(Class<T> target) {
    List<Owl<T>> rezOwls = new ArrayList<>();

    owls.forEach((key, value) -> {
      if (value.entityClass().equals(target)) {
        rezOwls.add((Owl<T>) value);
      }
    });
    return rezOwls;
  }

  public synchronized List<Owl<?>> getOwls() {

    return new ArrayList<>(owls.values());
  }

  public synchronized <T extends OwlEntity> Owl<T> createOwl(Class<T> target) throws IOException,
      JAXBException, ReflectiveOperationException, OwlEntityInitializeException {
    Owl<T> newOwl = Owl.create(ProjectPath.OWLERY.getPath(), target);
    initOwl(newOwl);
    owls.put(newOwl.info().id(), newOwl);

    notifyCreateOwlListeners(newOwl);

    return newOwl;
  }

  public synchronized <T extends OwlEntity> Owl<T> createOwl(Class<T> target, Owl<?> parent)
      throws IOException, JAXBException, ReflectiveOperationException,
      OwlEntityInitializeException {
    Owl<T> newOwl = createOwl(target);
    newOwl.head().setParentOwl(parent.info().id());

    return newOwl;
  }

  public synchronized <T extends OwlEntity> void deleteOwl(Owl<T> owl) throws IOException {
    Path location = owl.location();
    UUID owlID = owl.info().id();

    owl.close();
    Files.delete(location);
    owls.remove(owlID);

    notifyDeleteOwlListeners(owl);
  }

  public synchronized <T extends OwlEntity> Owl<T> duplicateOwl(Owl<T> owl) throws IOException,
      JAXBException, ReflectiveOperationException, OwlEntityInitializeException {

    Owl<T> newOwl = createOwl(owl.entityClass());
    newOwl.entity().syncWith(owl.entity());
    newOwl.head().syncWith(owl.head());

    notifyDuplicateOwlListeners(owl, newOwl);

    return newOwl;
  }

  public void addCreateOwlListener(CreateOwlListener listener) {
    createListeners.add(listener);
  }

  public void removeCreateOwlListener(CreateOwlListener listener) {
    createListeners.remove(listener);
  }

  public void addDeleteOwlListener(DeleteOwlListener listener) {
    deleteListeners.add(listener);
  }

  public void removeDeleteOwlListener(DeleteOwlListener listener) {
    deleteListeners.remove(listener);
  }

  public void addDuplicateOwlListener(DuplicateOwlListener listener) {
    duplicateListeners.add(listener);
  }

  public void removeDuplicateOwlListener(DuplicateOwlListener listener) {
    duplicateListeners.remove(listener);
  }

  private void notifyCreateOwlListeners(Owl<? extends OwlEntity> entity) {
    createListeners.forEach(i -> i.create(entity));
  }

  private void notifyDeleteOwlListeners(Owl<? extends OwlEntity> entity) {
    deleteListeners.forEach(i -> i.delete(entity));
  }

  private void notifyDuplicateOwlListeners(Owl<? extends OwlEntity> oldEntity,
      Owl<? extends OwlEntity> newEntity) {
    duplicateListeners.forEach(i -> i.duplicate(oldEntity, newEntity));
  }

  <T extends OwlEntity> Owl<T> loadOwl(UUID uuid, Class<T> target) throws OwlCastException,
      OwlNotFoundException, IOException, JAXBException, OwlEntityInitializeException {
    if (owls.containsKey(uuid)) {
      return getOwl(uuid, target);
    } else if (ref.containsKey(uuid)) {
      Owl<T> loadedOwl = new Owl<>(ref.get(uuid).location(), target);
      initOwl(loadedOwl);
      owls.put(uuid, loadedOwl);
      return loadedOwl;
    } else {
      throw new OwlNotFoundException();
    }
  }

  Owl<? extends OwlEntity> loadOwl(UUID uuid) throws OwlCastException, OwlNotFoundException,
      IOException, JAXBException, OwlEntityInitializeException, ClassNotFoundException {
    HollowOwl hollowOwl = ref.get(uuid);
    if (hollowOwl == null) {
      throw new OwlNotFoundException("Owl not found: " + uuid);
    }
    Optional<Class<? extends OwlEntity>> optTarget = findTarget(hollowOwl.info().targetClass());

    if (optTarget.isEmpty()) {
      throw new ClassNotFoundException();// TODO: Описание ошибки
    }
    return loadOwl(uuid, optTarget.get());
  }

  private void initOwl(Owl<?> owl) {
    owl.enableAutoSave(e -> {
      Logger.registerException(1, e);
    });
    owl.setAutoSaveDuration(2);
  }

  public void boot() throws IOException {
    if (isBoot)
      return;

    List<Path> owlFiles = Owls.findOwlFiles(ProjectPath.OWLERY.getPath(), 1);

    for (Path owlFile : owlFiles) {
      try {
        HollowOwl hollowOwl = Owl.getHollowOwl(owlFile);
        if (!validateFileName(hollowOwl)) {
          hollowOwl = rename(hollowOwl);
        }
        ref.put(hollowOwl.info().id(), hollowOwl);
      } catch (JAXBException | IOException e) {
        Logger.registerException(1, e);
      }
    }

    for (UUID uuid : ref.keySet()) {
      if (owls.containsKey(uuid)) {
        continue;
      }
      try {
        Owl<?> loadedOwl = loadOwl(uuid);
        owls.put(loadedOwl.info().id(), loadedOwl);
      } catch (OwlCastException | OwlNotFoundException | ClassNotFoundException | JAXBException
          | OwlEntityInitializeException e) {
        Logger.registerException(1, e);
        MessageBox messageBox = new MessageBox(AlertType.ERROR);
        messageBox.setTitle("Owl Load Error");
        messageBox.setHeaderText("An error occurred while loading Owl " + uuid);
        messageBox.setContentText(e.getMessage());
        messageBox.showAndWait();
        // TODO: Больше вариантов сообщения в зависимости от ошибки
      }
    }

    StageFactory.INSTANCE.addApplicationCloseAction(() -> {
      owls.forEach((id, owl) -> {
        try {
          owl.close();
        } catch (IOException e) {
          Logger.registerException(1, e);
        }
      });
    });

    isBoot = true;

  }

  private HollowOwl rename(HollowOwl hollowOwl) throws IOException, JAXBException {
    Path newPath =
        ProjectPath.OWLERY.getPath().resolve(hollowOwl.info().id().toString() + Owl.EXTENSION);
    Files.move(hollowOwl.location(), newPath);
    // TODO: Уведомление о переименовании
    return Owl.getHollowOwl(newPath);
  }

  private boolean validateFileName(HollowOwl hollowOwl) {
    String fileName = hollowOwl.location().getFileName().toString();
    String mustBeFileName = hollowOwl.info().id().toString() + Owl.EXTENSION;
    return fileName.equals(mustBeFileName);
  }

  private static Optional<Class<? extends OwlEntity>> findTarget(String className) {
    Optional<OwlEntity> optTarget = ModuleLoader.INSTANCE.loadOwlEntity(className);
    if (optTarget.isPresent()) {
      return Optional.of(optTarget.get().getClass());
    } else {
      return Optional.empty();
    }
  }
}
