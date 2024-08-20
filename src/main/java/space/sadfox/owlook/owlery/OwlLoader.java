package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import space.sadfox.owlook.base.owl.HollowOwl;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlEntityInitializeException;
import space.sadfox.owlook.base.owl.Owls;
import space.sadfox.owlook.moduleloader.ModuleLoader;
import space.sadfox.owlook.owlery.DependencyBuilder.Dependency;
import space.sadfox.owlook.ui.tools.MessageBox;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;
import space.sadfox.owlook.utils.ProjectPath;

public enum OwlLoader {
  INSTANCE;

  public static enum DeleteFlag {
    FORCE, NO_DEPENDENCIES;
  }

  @FunctionalInterface
  public static interface CreateOwlListener {
    void create(Owl<?> entity);
  }

  @FunctionalInterface
  public static interface DeleteOwlListener {
    void delete(Owl<?> entity);
  }

  @FunctionalInterface
  public static interface DuplicateOwlListener {
    void duplicate(Owl<?> oldEntity, Owl<?> newEntity);
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

  public synchronized <T extends OwlEntity> List<Owl<T>> getOwls(Class<T> target) {
    return getAllOwls(target).stream().filter(owl -> !owl.head().isHidden())
        .collect(Collectors.toList());
  }

  public synchronized List<Owl<?>> getOwls() {
    return getAllOwls().stream().filter(owl -> !owl.head().isHidden()).collect(Collectors.toList());
  }

  synchronized List<Owl<?>> getAllOwls() {
    return new ArrayList<>(owls.values());
  }

  @SuppressWarnings("unchecked")
  synchronized <T extends OwlEntity> List<Owl<T>> getAllOwls(Class<T> target) {
    List<Owl<T>> rezOwls = new ArrayList<>();

    owls.forEach((key, value) -> {
      if (value.entityClass().equals(target)) {
        rezOwls.add((Owl<T>) value);
      }
    });
    return rezOwls;
  }

  public synchronized <T extends OwlEntity> Owl<T> createOwl(Class<T> target) throws IOException,
      JAXBException, ReflectiveOperationException, OwlEntityInitializeException {
    Owl<T> newOwl = Owl.create(ProjectPath.OWLERY.getPath(), target);
    initOwl(newOwl);
    owls.put(newOwl.info().id(), newOwl);

    notifyCreateOwlListeners(newOwl);

    return newOwl;
  }

  public synchronized <T extends OwlEntity> Owl<T> createHiddenOwl(Class<T> target)
      throws IOException, JAXBException, ReflectiveOperationException,
      OwlEntityInitializeException {
    Owl<T> newOwl = Owl.create(ProjectPath.OWLERY.getPath(), target);
    initOwl(newOwl);
    owls.put(newOwl.info().id(), newOwl);
    newOwl.head().setHidden(true);

    return newOwl;
  }

  public synchronized void deleteOwl(Owl<?> owl, List<Owl<?>> ignoreDependencies,
      DeleteFlag... flags) throws IOException {
    boolean depFlag = false, deleteConfyrm = false;

    for (DeleteFlag flag : flags) {
      switch (flag) {
        case FORCE:
          deleteConfyrm = true;
          break;
        case NO_DEPENDENCIES:
          depFlag = true;
          break;
      }
    }

    if (!deleteConfyrm) {
      DependencyBuilder depBuilder = new DependencyBuilder(getAllOwls());
      List<Owl<?>> dependencyFor = new ArrayList<>();
      Optional<Dependency> o = depBuilder.getDendency(owl);
      if (o.isPresent()) {
        dependencyFor = o.get().getDependencyFor().stream()
            .filter(depOwl -> !ignoreDependencies.contains(depOwl)).collect(Collectors.toList());
      }

      MessageBox messageBox =
          new MessageBox(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
      messageBox.setTitle("Deletion confirmation");
      messageBox.setHeaderText("Are you sure you want to remove " + owl.info().owlName() + ": "
          + owl.head().getTitle() + "?");
      if (dependencyFor.size() > 0) {
        StringBuilder depMessage = new StringBuilder("This Owl is an dependency for:\n");
        dependencyFor.forEach(depOwl -> {
          depMessage.append(" -" + depOwl.info().owlName() + ": " + depOwl.head().getTitle());
          depMessage.append("\n");
        });
        messageBox.setContentText(depMessage.toString());
      }

      if (depFlag && (dependencyFor.size() == 0)) {
        deleteConfyrm = true;
      } else {
        Optional<ButtonType> answer = messageBox.showAndWait();
        if (answer.isPresent() && answer.get().equals(ButtonType.YES)) {
          deleteConfyrm = true;
        }
      }
    }

    if (deleteConfyrm) {
      Path location = owl.location();
      UUID owlID = owl.info().id();
      Files.delete(location);
      owls.remove(owlID);
      if (!owl.head().isHidden()) {
        notifyDeleteOwlListeners(owl);
      }
    }
  }

  public synchronized void deleteOwl(Owl<?> owl, DeleteFlag... flags) throws IOException {
    deleteOwl(owl, new ArrayList<>(), flags);
  }

  public synchronized <T extends OwlEntity> Owl<T> duplicateOwl(Owl<T> owl) throws IOException,
      JAXBException, ReflectiveOperationException, OwlEntityInitializeException {

    Owl<T> newOwl = null;
    boolean isHidden = owl.head().isHidden();
    if (isHidden) {
      newOwl = createHiddenOwl(owl.entityClass());
    } else {
      newOwl = createOwl(owl.entityClass());
    }
    newOwl.syncWith(owl);

    if (!isHidden) {
      notifyDuplicateOwlListeners(owl, newOwl);
    }

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

  private void notifyCreateOwlListeners(Owl<?> entity) {
    createListeners.forEach(i -> i.create(entity));
  }

  private void notifyDeleteOwlListeners(Owl<?> entity) {
    deleteListeners.forEach(i -> i.delete(entity));
  }

  private void notifyDuplicateOwlListeners(Owl<?> oldEntity, Owl<?> newEntity) {
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
      throw new OwlNotFoundException("ID: " + uuid);
    }
  }

  Owl<?> loadOwl(UUID uuid) throws OwlCastException, OwlNotFoundException, IOException,
      JAXBException, OwlEntityInitializeException, ClassNotFoundException {
    HollowOwl hollowOwl = ref.get(uuid);
    if (hollowOwl == null) {
      throw new OwlNotFoundException("Owl not found: " + uuid);
    }
    Optional<Class<? extends OwlEntity>> optTarget = findTarget(hollowOwl.info().targetClass());

    if (optTarget.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Provider was not found\n\n").append("Title: " + hollowOwl.head().getTitle())
          .append('\n').append("ID: " + hollowOwl.info().id().toString()).append('\n')
          .append("Name: " + hollowOwl.info().owlName()).append('\n')
          .append("Module: " + hollowOwl.info().createdModule());
      throw new ClassNotFoundException(builder.toString());
    }
    return loadOwl(uuid, optTarget.get());
  }

  private void initOwl(Owl<?> owl) {
    owl.enableAutoSave(e -> {
      Owlook.registerException(e);
    });
    owl.setAutoSaveDelay(2);
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
      } catch (FileAlreadyExistsException e) {
      } catch (JAXBException | IOException e) {
        Owlook.registerException(e);
      }
    }

    for (UUID uuid : ref.keySet()) {
      if (owls.containsKey(uuid)) {
        continue;
      }
      try {
        Owl<?> loadedOwl = loadOwl(uuid);
        owls.put(loadedOwl.info().id(), loadedOwl);
      } catch (OwlCastException | OwlNotFoundException | ClassNotFoundException | IOException
          | JAXBException | OwlEntityInitializeException e) {
        Owlook.registerException(e);
        Owlook.notificate(generateMessage(e));
      }
    }
    DependencyBuilder depBuilder = new DependencyBuilder(getAllOwls());
    Set<Owl<?>> forgottenOwls = depBuilder.getForgottenOwls();
    if (forgottenOwls.size() > 0) {
      forgottenOwls.forEach(forgottenOwl -> {
        try {
          deleteOwl(forgottenOwl, DeleteFlag.FORCE);
        } catch (IOException e) {
          Owlook.registerException(e);
        }
      });
    }
    isBoot = true;

  }

  private HollowOwl rename(HollowOwl hollowOwl)
      throws FileAlreadyExistsException, IOException, JAXBException {
    Path newPath =
        ProjectPath.OWLERY.getPath().resolve(hollowOwl.info().id().toString() + Owl.EXTENSION);
    if (Files.exists(newPath)) {
      // TODO: MassageBox уведомляющий что в папке существует дубликат сущности и она не будет
      // загружена или диалог предлагающий удалить дубликат
      throw new FileAlreadyExistsException("");
    }
    Files.move(hollowOwl.location(), newPath);
    // TODO: Уведомление о переименовании
    return Owl.getHollowOwl(newPath);
  }

  private boolean validateFileName(HollowOwl hollowOwl) {
    String fileName = hollowOwl.location().getFileName().toString();
    String mustBeFileName = hollowOwl.info().id().toString() + Owl.EXTENSION;
    return fileName.equals(mustBeFileName);
  }

  static Optional<Class<? extends OwlEntity>> findTarget(String className) {
    Optional<OwlEntity> optTarget = ModuleLoader.INSTANCE.loadOwlEntity(className);
    if (optTarget.isPresent()) {
      return Optional.of(optTarget.get().getClass());
    } else {
      return Optional.empty();
    }
  }

  static OwlookMessage generateMessage(Exception e) {
    OwlookMessage m = null;
    if (e instanceof OwlNotFoundException) {
      m = new OwlookMessage(MessageLevel.WARNING);
      m.setName("Owl Not Found");
    } else if (e instanceof ClassNotFoundException) {
      m = new OwlookMessage(MessageLevel.WARNING);
      m.setName("Owl Not Load");
    } else if (e instanceof OwlEntityInitializeException) {
      m = new OwlookMessage(MessageLevel.ERROR);
      m.setName("An error occurred while initializing Owl");
    } else {
      m = new OwlookMessage(MessageLevel.ERROR);
      m.setName("Owl Load Error");
    }
    m.setMessage(e.getMessage());
    return m;
  }
}
