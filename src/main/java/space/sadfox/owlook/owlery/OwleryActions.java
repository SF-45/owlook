package space.sadfox.owlook.owlery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import jakarta.xml.bind.JAXBException;
import space.sadfox.owlook.base.owl.Owl;
import space.sadfox.owlook.base.owl.OwlEntity;
import space.sadfox.owlook.base.owl.OwlEntityInitializeException;
import space.sadfox.owlook.owlery.OwlLoader.DeleteFlag;
import space.sadfox.owlook.ui.base.Controllable;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public class OwleryActions {

  private OwleryActions() {

  }

  public static void createOwlAction(Class<? extends OwlEntity> target) {
    try {
      Owl<?> newOwl = OwlLoader.INSTANCE.createOwl(target);
      editOwlAction(newOwl);
    } catch (Exception e) {
      Owlook.registerException(e);
    }
  }

  public static void editOwlAction(Owl<?> owl) {
    if (owl.entity() instanceof Controllable) {
      Controllable controlEntity = (Controllable) owl.entity();
      try {
        controlEntity.getController().show();
      } catch (Exception e) {
        Owlook.registerException(e);
      }
    }
  }

  public static void duplicateOwlAction(Owl<?> owl) {
    try {
      OwlLoader.INSTANCE.duplicateOwl(owl);
    } catch (IOException | JAXBException | ReflectiveOperationException
        | OwlEntityInitializeException e) {
      Owlook.registerException(e);
    }
  }

  public static void deleteOwlAction(List<Owl<?>> owls) {
    List<Owl<?>> deleteOwls = new ArrayList<>(owls);
    OwlLoader loader = OwlLoader.INSTANCE;
    BiConsumer<IOException, Owl<?>> exceptionHandler = (e, owl) -> {
      Owlook.registerException(e);

      OwlookMessage message = new OwlookMessage(MessageLevel.ERROR,
          "Deletion error " + owl.info().owlName() + ": " + owl.head().getTitle(),
          e.getClass().getSimpleName() + ": " + e.getMessage());
      Owlook.notificate(message);

    };
    if (deleteOwls.size() == 1) {
      try {
        loader.deleteOwl(deleteOwls.get(0));
      } catch (IOException e) {
        exceptionHandler.accept(e, deleteOwls.get(0));
      }

    } else if (deleteOwls.size() > 1) {
      deleteOwls.forEach(owl -> {
        try {
          loader.deleteOwl(owl, DeleteFlag.NO_DEPENDENCIES);
        } catch (IOException e) {
          exceptionHandler.accept(e, owl);
        }
      });
    }
  }

}
