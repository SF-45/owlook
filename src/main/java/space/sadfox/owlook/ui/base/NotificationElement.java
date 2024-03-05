package space.sadfox.owlook.ui.base;

import org.kordamp.ikonli.codicons.Codicons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import space.sadfox.owlook.utils.MessageLevel;
import space.sadfox.owlook.utils.OwlookMessage;

public class NotificationElement {

  private final double MAX_HEIGHT = 100d;

  private final GridPane root = new GridPane();
  private final FontIcon icon = new FontIcon();
  private final HBox actionIcons = new HBox();
  private final Label title = new Label();
  private final Label message = new Label();
  private final ProgressBar progressBar = new ProgressBar();
  private final FontIcon closeButton = new FontIcon(Codicons.CLOSE);
  private final BooleanProperty expanded = new SimpleBooleanProperty(false);

  public NotificationElement(String title, String message, MessageLevel type) {
    decorate();
    this.title.setText(title);
    this.message.setText(message);
    icon.setIconCode(type.getIkon());
    icon.setIconColor(type.getPaint());
  }

  public NotificationElement(OwlookMessage notificationMessage) {
    this(notificationMessage.getName(), notificationMessage.getMessage(),
        notificationMessage.getMessageLevel());
  }

  public NotificationElement() {
    decorate();
  }

  // TODO: Сделать стилизацию через CSS
  private void decorate() {
    root.setMaxHeight(MAX_HEIGHT);
    expanded.addListener((property, oldValue, newValue) -> {
      if (newValue) {
        root.setMaxHeight(GridPane.USE_COMPUTED_SIZE);
      } else {
        root.setMaxHeight(MAX_HEIGHT);
      }
    });

    root.setStyle("-fx-background-color: f4f4f4; -fx-background-radius: 10;");

    ColumnConstraints column0 = new ColumnConstraints();
    column0.setHgrow(Priority.NEVER);
    ColumnConstraints column1 = new ColumnConstraints();
    column1.setHgrow(Priority.ALWAYS);

    RowConstraints row0 = new RowConstraints();
    row0.setVgrow(Priority.NEVER);
    RowConstraints row1 = new RowConstraints();
    row1.setVgrow(Priority.ALWAYS);
    RowConstraints row2 = new RowConstraints();
    row2.setVgrow(Priority.NEVER);

    root.getColumnConstraints().addAll(column0, column1);
    root.getRowConstraints().addAll(row0, row1, row2);

    icon.setIconSize(40);
    GridPane.setMargin(icon, new Insets(10d));
    GridPane.setRowSpan(icon, GridPane.REMAINING);
    root.add(icon, 0, 0);

    title.setFont(new Font("System Bold", 17d));
    GridPane.setMargin(title, new Insets(4d, 0d, 0d, 0d));
    GridPane.setValignment(title, VPos.TOP);
    root.add(title, 1, 0);

    message.setWrapText(true);
    message.setFont(new Font(13d));
    message.setFont(new Font("System", 13d));
    GridPane.setValignment(message, VPos.TOP);
    root.add(message, 1, 1);

    progressBar.setPrefHeight(9d);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.setStyle("-fx-border-insets: 0; -fx-border-style: none; -fx-background-radius: 0;");
    GridPane.setColumnSpan(progressBar, GridPane.REMAINING);
    GridPane.setMargin(progressBar, new Insets(0d, 4d, 0d, 4d));
    GridPane.setValignment(progressBar, VPos.BOTTOM);
    root.add(progressBar, 0, 2);

    // ICONS
    actionIcons.setAlignment(Pos.TOP_RIGHT);
    actionIcons.setSpacing(3d);
    GridPane.setHalignment(actionIcons, HPos.RIGHT);
    GridPane.setValignment(actionIcons, VPos.TOP);
    GridPane.setMargin(actionIcons, new Insets(4d, 4d, 0d, 0d));
    root.add(actionIcons, 1, 0);

    addActionIcon(closeButton);

    FontIcon expand = new FontIcon(Codicons.CHEVRON_DOWN);
    expand.setOnMousePressed(event -> {
      if (!expanded.get()) {
        expand.setIconCode(Codicons.CHEVRON_UP);
        expanded.set(true);
      } else {
        expand.setIconCode(Codicons.CHEVRON_DOWN);
        expanded.set(false);
      }
    });
    addActionIcon(expand);
  }

  public void bindProgressBar(Task<Void> task) {
    progressBar.progressProperty().bind(task.progressProperty());
    closeButton.setOnMousePressed(event -> task.cancel());
  }

  public Node getRoot() {
    return root;
  }

  public StringProperty titleProperty() {
    return title.textProperty();
  }

  public String getTitle() {
    return title.getText();
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public StringProperty messageProperty() {
    return message.textProperty();
  }

  public String getMessage() {
    return message.getText();
  }

  public void setMessage(String message) {
    this.message.setText(message);
  }

  public FontIcon getFontIcon() {
    return icon;
  }

  public void addActionIcon(FontIcon icon) {
    icon.setIconSize(14);
    icon.setOnMouseEntered(mouseEvent -> {
      icon.setFill(Color.DARKGRAY);
    });
    icon.setOnMouseExited(mouseEvent -> {
      icon.setFill(Color.BLACK);
    });
    actionIcons.getChildren().add(0, icon);
  }

  public void removeActionIcon(FontIcon icon) {
    actionIcons.getChildren().remove(icon);
  }

}
