package space.sadfox.owlook.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import space.sadfox.owlook.ui.base.NotificationElement;
import space.sadfox.owlook.utils.NotificationPos;
import space.sadfox.owlook.utils.Owlook;
import space.sadfox.owlook.utils.OwlookMessage;

public class NotificationPopup {
  private final Popup popupStage;
  private final Stage crutchStage;
  private final ObjectProperty<NotificationPos> position;

  private final BooleanProperty pauseFlag = new SimpleBooleanProperty(false);

  private final VBox root;

  public NotificationPopup() {
    popupStage = new Popup();
    popupStage.setAutoFix(false);
    popupStage.getScene().setFill(Color.TRANSPARENT);
    crutchStage = new Stage(StageStyle.UTILITY);
    crutchStage.setMaxWidth(0d);
    crutchStage.setMaxHeight(0d);
    crutchStage.setOpacity(0d);
    crutchStage.setX(Double.MAX_VALUE);

    root = new VBox();
    root.setPrefWidth(350d);
    root.setMinWidth(GridPane.USE_PREF_SIZE);
    root.setMaxWidth(GridPane.USE_PREF_SIZE);
    root.setSpacing(10d);
    root.setFillWidth(true);
    popupStage.getContent().add(root);

    root.getChildren().addListener((InvalidationListener) change -> {
      if (root.getChildren().size() == 0) {
        hide();
      } else {
        show();
      }
    });

    position = Owlook.getConfig().notificationPosProperty();
    position.addListener((InvalidationListener) change -> {
      updateXPosition(root.getHeight());
      updateYPosition(root.getWidth());
    });

    root.setOnMouseEntered(mouseEvent -> pauseFlag.set(true));
    root.setOnMouseExited(mouseEvent -> pauseFlag.set(false));
    root.heightProperty().addListener((property, oldValue, newValue) -> {
      updateYPosition(newValue.doubleValue());
    });
    root.widthProperty().addListener((property, oldValue, newValue) -> {
      updateXPosition(newValue.doubleValue());
    });
  }

  public synchronized void showMessage(OwlookMessage message) {
    NotificationElement element = new NotificationElement(message);
    showMessage(element);
  }

  public synchronized void showMessage(NotificationElement element) {
    Task<Void> timer = new Task<>() {
      @Override
      protected Void call() {
        double i = 0, time = 5000;
        while (i < time) {
          updateProgress(i, time);
          if (!pauseFlag.get()) {
            i++;
          }
          if (isCancelled()) {
            return null;
          }
          try {
            Thread.sleep(1);
          } catch (Exception e) {
          }
        }
        succeeded();
        return null;
      }
    };
    timer.stateProperty().addListener((property, oldValue, newValue) -> {
      switch (newValue) {
        case SUCCEEDED:
        case CANCELLED:
          root.getChildren().remove(element.getRoot());
          break;
        default:
          break;
      }

    });
    element.bindProgressBar(timer);
    root.getChildren().add(element.getRoot());

    Thread tr = new Thread(timer);
    tr.setDaemon(true);
    tr.start();
  }

  private synchronized void hide() {
    if (popupStage.isShowing()) {
      popupStage.hide();
      crutchStage.hide();
    }
  }

  private synchronized void show() {
    if (!crutchStage.isShowing()) {
      crutchStage.show();
    }
    if (!popupStage.isShowing()) {
      popupStage.show(crutchStage);
    }
  }

  private synchronized void updateXPosition(double width) {
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    double xInd = 0.025d, xPos = 0d;

    switch (position.get()) {
      case TOP_LEFT:
        xPos = bounds.getMinX() + (bounds.getWidth() * xInd);
        break;
      case TOP_RIGHT:
        xPos = bounds.getMaxX() - popupStage.getWidth() - (bounds.getWidth() * xInd);
        break;
      case BOTTOM_LEFT:
        xPos = bounds.getMinX() + (bounds.getWidth() * xInd);
        break;
      case BOTTOM_RIGHT:
        xPos = bounds.getMaxX() - popupStage.getWidth() - (bounds.getWidth() * xInd);
        break;
    }
    popupStage.setX(xPos);
  }

  private synchronized void updateYPosition(double heigth) {
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    double yInd = 0.03d, yPos = 0d;


    switch (position.get()) {
      case TOP_LEFT:
        yPos = bounds.getMinY() + (bounds.getHeight() * yInd);
        break;
      case TOP_RIGHT:
        yPos = bounds.getMinY() + (bounds.getHeight() * yInd);
        break;
      case BOTTOM_LEFT:
        yPos = bounds.getMaxY() - heigth - (bounds.getHeight() * yInd);
        break;
      case BOTTOM_RIGHT:
        yPos = bounds.getMaxY() - heigth - (bounds.getHeight() * yInd);
        break;
    }
    popupStage.setY(yPos);
  }
}
