package space.sadfox.owlook.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public enum StageFactory {
    INSTANCE;

    private final ObservableList<Stage> openStages = FXCollections.observableArrayList();

    public ObservableList<Stage> getOpenStages() {
        return openStages;
    }

    private final ObjectProperty<Stage> currentStage = new SimpleObjectProperty<>(null);

    private final  ObservableMap<EventHandler<KeyEvent>, EventType<KeyEvent>> keyEvents = FXCollections.observableHashMap();

    public final ObjectProperty<Stage> currentStageProperty() {
        return this.currentStage;
    }

    public final Stage getCurrentStage() {
        return this.currentStageProperty().get();
    }

    public final void setCurrentStage(final Stage currentStage) {
        this.currentStageProperty().set(currentStage);
    }

    public void registerStage(Stage stage) {
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e ->
                openStages.add(stage));
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, e ->
                openStages.remove(stage));
        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                currentStage.set(stage);
            } else {
                currentStage.set(null);
            }
        });
        keyEvents.forEach((eventHandler, eventType) -> {
            stage.addEventFilter(eventType, eventHandler);
        });
        keyEvents.addListener((MapChangeListener<EventHandler<KeyEvent>, EventType<KeyEvent>>) change -> {
            if (change.wasAdded()) {
                stage.addEventFilter(change.getValueAdded(), change.getKey());
            } else if (change.wasRemoved()) {
                stage.removeEventFilter(change.getValueAdded(), change.getKey());
            }
        });
    }

    public Stage createStage() {
        Stage stage = new Stage();
        registerStage(stage);
        return stage;
    }

    public ObservableMap<EventHandler<KeyEvent>, EventType<KeyEvent>> getKeyEvents() {
        return keyEvents;
    }
}
