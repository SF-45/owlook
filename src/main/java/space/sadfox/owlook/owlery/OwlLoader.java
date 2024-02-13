package space.sadfox.owlook.owlery;

import java.io.FileNotFoundException;
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
  private boolean isBoot = false;

  private final List<CreateOwlListener> createListeners = new ArrayList<>();
  private final List<DeleteOwlListener> deleteListeners = new ArrayList<>();
  private final List<DuplicateOwlListener> duplicateListeners = new ArrayList<>();

  public <T extends OwlEntity> Owl<T> getOwl(UUID uuid, Class<T> target)
      throws OwlCastException, OwlNotFoundException {
    if (!owls.containsKey(uuid)) {
      throw new OwlNotFoundException("Owl not found: " + uuid);
    }
    Owl owl = owls.get(uuid);
    if (!owl.entityClass().equals(target)) {
      throw new OwlCastException("Can't to cast an owl to" + target.getSimpleName());
    }
    return owl;
  }

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
      throws Exception {
    Owl<T> newOwl = createOwl(target);
    newOwl.head().setParentOwl(parent.info().id());

    return newOwl;
  }

  public synchronized <T extends OwlEntity> void deleteOwl(Owl<T> owl) throws Exception {

    Path location = owl.location();
    UUID owlID = convertPathToUUID(location);

    owl.close();
    owls.remove(owlID);
    Files.delete(location);

    notifyDeleteOwlListeners(owl);
  }

  public synchronized <T extends OwlEntity> Owl<T> duplicateOwl(Owl<T> owl) throws Exception {

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

  Owl<? extends OwlEntity> loadOwl(Path path)
      throws IOException, JAXBException, ClassNotFoundException, OwlCastException,
      OwlNotFoundException, OwlEntityInitializeException, Exception {
    HollowOwl hollowOwl = Owl.getHollowOwl(path);

    Optional<Class<? extends OwlEntity>> optTarget = findTarget(hollowOwl.info().targetClass());

    if (optTarget.isEmpty()) {
      throw new ClassNotFoundException();// TODO: Описание ошибки
    }
    hollowOwl.close();

    return loadOwl(path, optTarget.get());
  }

  <T extends OwlEntity> Owl<T> loadOwl(Path path, Class<T> target) throws OwlCastException,
      OwlNotFoundException, IOException, JAXBException, OwlEntityInitializeException, Exception {

    path = validate(path);
    UUID owlID = convertPathToUUID(path);
    if (owls.containsKey(owlID)) {
      return getOwl(owlID, target);
    } else {
      Owl<T> loadedOwl = new Owl<>(path, target);
      initOwl(loadedOwl);
      owls.put(owlID, loadedOwl);
      return loadedOwl;
    }
  }

  <T extends OwlEntity> Owl<T> loadOwl(String uuid, Class<T> target) throws OwlCastException,
      OwlNotFoundException, IOException, JAXBException, OwlEntityInitializeException, Exception {

    Path path = validate(uuid);
    return loadOwl(path, target);
  }

  private Path validate(String fileName) throws IOException, JAXBException, Exception {
    if (!fileName.endsWith(Owl.EXTENSION)) {
      fileName += Owl.EXTENSION;
    }

    return validate(ProjectPath.OWLERY.getPath().resolve(fileName));

  }

  private Path validate(Path path) throws IOException, JAXBException, Exception {
    if (Files.notExists(path)) {
      throw new FileNotFoundException("Owl " + path + Owl.EXTENSION + " does not exist");
    }
    if (!path.toAbsolutePath().getParent().equals(ProjectPath.OWLERY.getPath())) {
      throw new IOException("Owl " + path + " whitout directory " + ProjectPath.OWLERY.getPath());
    }
    if (!isUUID(path)) {
      path = renameOwl(path);
    }

    return path;
  }

  private Path renameOwl(Path owlPath) throws IOException, JAXBException, Exception {
    try (HollowOwl hollowOwl = Owl.getHollowOwl(owlPath)) {
      Path outPath =
          ProjectPath.OWLERY.getPath().resolve(hollowOwl.info().id().toString() + Owl.EXTENSION);
      Files.move(hollowOwl.location(), outPath);
      // TODO: Уведомление о переименовании
      return outPath;
    }
  }

  private void initOwl(Owl<?> owl) {
    owl.enableAutoSave(e -> {
      Logger.registerException(1, e);
    });
  }

  public void boot() throws IOException {
    if (isBoot)
      return;

    List<Path> owlFiles = Owls.findOwlFiles(ProjectPath.OWLERY.getPath(), 1);
    owlFiles.forEach(t -> {
      try {
        OwlLoader.INSTANCE.loadOwl(t);
      } catch (Exception e) {
        Logger.registerException(1, e);
        MessageBox messageBox = new MessageBox(AlertType.ERROR);
        messageBox.setTitle("Owl Load Error");
        messageBox.setHeaderText("An error occurred while loading Owl " + t);
        messageBox.setContentText(e.getMessage());
        messageBox.showAndWait();
      }
    });

    StageFactory.INSTANCE.addApplicationCloseAction(() -> {
      owls.forEach((id, owl) -> {
        try {
          owl.close();
        } catch (IOException e) {
          Logger.registerException(1, e);
        }
      });
    });

  }

  public static String convertPathToString(Path path) {
    String fileName = path.getFileName().toString();
    int ind = fileName.lastIndexOf(".");
    return fileName.substring(0, ind);
  }

  public static Optional<Class<? extends OwlEntity>> findTarget(String className) {
    Optional<OwlEntity> optTarget = ModuleLoader.INSTANCE.loadOwlEntity(className);
    if (optTarget.isPresent()) {
      return Optional.of(optTarget.get().getClass());
    } else {
      return Optional.empty();
    }
  }

  public static UUID convertPathToUUID(Path path) {
    return UUID.fromString(convertPathToString(path));
  }

  public static boolean isUUID(Path path) {
    try {
      UUID.fromString(convertPathToString(path));
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
