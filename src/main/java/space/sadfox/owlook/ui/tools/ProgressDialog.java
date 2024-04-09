package space.sadfox.owlook.ui.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import space.sadfox.owlook.ui.base.FormDesigners;

public class ProgressDialog extends Dialog<ButtonType> {

  private final DialogPane dialogPane;
  private final VBox content;
  private final Label progressText;
  private final TextArea details;
  private final ProgressBar progressBar;

  private final ButtonType cancel = ButtonType.CANCEL;
  private final ButtonType done = ButtonType.FINISH;
  private DateFormat dataFormat;
  private long startTime;
  private final StringProperty titleWhithProgress = new SimpleStringProperty("Progress");

  public ProgressDialog(final Task<?> task) {
    dataFormat = new SimpleDateFormat("HH:mm:ss");

    dialogPane = new DialogPane();
    dialogPane.setMinWidth(500d);
    dialogPane.setMaxWidth(700d);
    dialogPane.setHeaderText("Progress somthing");

    dialogPane.getButtonTypes().add(cancel);
    this.setDialogPane(dialogPane);

    content = new VBox();
    dialogPane.setContent(content);
    content.setFillWidth(true);
    content.setSpacing(10d);
    content.setPadding(new Insets(10d));

    AnchorPane progressBarResizer = FormDesigners.addTo(content, new AnchorPane());
    progressBar = FormDesigners.addTo(progressBarResizer, new ProgressBar());
    FormDesigners.setAnchors(new Insets(0d), progressBar);
    progressText = FormDesigners.addTo(content, new Label());

    details = new TextArea();
    dialogPane.setExpandableContent(details);
    details.setEditable(false);


    progressText.textProperty().addListener((property, oldValue, newValue) -> {
      Date date = new Date(System.currentTimeMillis());
      details.setText(dataFormat.format(date) + ": " + newValue + '\n' + details.getText());
    });

    StringBinding titleBinding = Bindings.createStringBinding(() -> {
      double proc = Math.round(progressBar.getProgress() * 10000.0) / 100.0;
      return titleWhithProgress.get() + " " + proc + "%";
    }, titleWhithProgress, progressBar.progressProperty());
    this.titleProperty().bind(titleBinding);

    setOnShown(event -> {
      startTime = System.currentTimeMillis();
    });

    setOnCloseRequest(event -> {
      if (getResult() == null || getResult().equals(ButtonType.CANCEL)) {
        task.cancel();
        event.consume();
      }
    });
    registerTask(task);
  }

  private void finish() {
    dialogPane.getButtonTypes().removeAll(cancel);
    dialogPane.getButtonTypes().add(done);
  }

  private void registerTask(Task<?> task) {
    progressBar.progressProperty().bind(task.progressProperty());
    progressText.textProperty().bind(task.messageProperty());
    task.setOnSucceeded(event -> {
      long doneTime = System.currentTimeMillis() - startTime;

      progressText.textProperty().unbind();
      progressText.setText(
          "Done for " + new SimpleDateFormat("mm'm' ss's' SSSS'ms'").format(new Date(doneTime)));
      finish();
    });
    task.setOnCancelled(event -> {
      finish();
    });
    task.setOnFailed(event -> {
      Throwable e = task.getException();
      progressText.textProperty().unbind();
      progressText.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
      finish();
    });
  }

  public ReadOnlyDoubleProperty progressProperty() {
    return progressBar.progressProperty();
  }

  public double getProgress() {
    return progressBar.getProgress();
  }

  public ReadOnlyStringProperty progressTextProperty() {
    return progressText.textProperty();
  }

  public String getProgressText() {
    return progressText.getText();
  }

  public DateFormat getDateFormat() {
    return this.dataFormat;
  }

  public void setDateFormat(DateFormat dateFormat) {
    this.dataFormat = dateFormat;
  }

  public StringProperty titleWhithProgressProperty() {
    return titleWhithProgress;
  }

  public String getTitleWhithProgress() {
    return titleWhithProgress.get();
  }

  public void setTitleWhithProgress(String titleWhithProgress) {
    this.titleWhithProgress.set(titleWhithProgress);
  }
}
