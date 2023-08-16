package space.sadfox.owlook.ui.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

public abstract class NumberField<T extends Number> extends TextField {

	private static class MinMaxNotSet extends Exception {

		private static final long serialVersionUID = 8779766541738936933L;
	}

	private final ObjectProperty<T> valueProperty = new SimpleObjectProperty<>();
	private final T minValue;
	private final T maxValue;

	public NumberField(T initValue) {
		this(initValue, null, null);
	}

	public NumberField(T initValue, T minValue, T maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;

		textProperty().bindBidirectional(valueProperty, stringConverter());
		setTextFormatter(new TextFormatter<>(change -> {
			boolean inputFilter = false;
			boolean rezultTextFilter = false;
			
			if (inputTextFilter(change.getText()) == null) {
				inputFilter = defaultInputTextFilter(change.getText());
			} else {
				inputFilter = inputTextFilter(change.getText());
			}
			
			if (rezultTextFilter(change.getControlNewText()) == null) {
				rezultTextFilter = defaultRezultTextFilter(change.getControlNewText());
			} else {
				rezultTextFilter = rezultTextFilter(change.getControlNewText());
			}
			
			if (change.getText().isEmpty()) {
				return change;
			} else if (!inputFilter || !rezultTextFilter) {
				return null;
			} else {
				try {
					T max = getMax();
					T i = stringConverter().fromString(change.getControlNewText());
					if (i == null) {
						i = max;
					}
					if (i.doubleValue() >= max.doubleValue()) {
						change.setRange(0, change.getControlText().length());
						change.setText(stringConverter().toString(i));
					}
				} catch (MinMaxNotSet e) {
				}
				return change;
			}
		}));
		Runnable minValidate = () -> {
			try {
				T min = getMin();
				T i = stringConverter().fromString(getText());
				if (i == null) {
					i = min;
				}
				if (i.doubleValue() <= min.doubleValue()) {
					setText(stringConverter().toString(i));
				}
			} catch (MinMaxNotSet e) {
			}
		};
		setOnAction(event -> minValidate.run());
		addEventHandler(ActionEvent.ACTION, event -> minValidate.run());
		focusedProperty().addListener(property -> minValidate.run());
		valueProperty.set(initValue);
	}

	private T getMin() throws MinMaxNotSet {
		if (minValue == null) {
			throw new MinMaxNotSet();
		}
		return minValue;
	}

	private T getMax() throws MinMaxNotSet {
		if (maxValue == null) {
			throw new MinMaxNotSet();
		}
		return maxValue;
	}

	private boolean defaultInputTextFilter(String newInput) {
		return newInput.matches("[0-9/.]+");
	};
	private boolean defaultRezultTextFilter(String newText) {
		return newText.chars().filter(ch -> ch == '.').count() > 1 ? false : true;
	};

	protected abstract StringConverter<T> stringConverter();

	protected abstract Boolean inputTextFilter(String newInput);
	protected abstract Boolean rezultTextFilter(String newText);

	protected T normolize(T current) {
		try {
			if (current.doubleValue() < getMin().doubleValue()) {
				return getMin();
			}
		} catch (MinMaxNotSet e) {
		}
		try {
			if (current.doubleValue() > getMax().doubleValue()) {
				return getMax();
			}
		} catch (MinMaxNotSet e) {
		}
		return current;
	}

	public T getValue() {
		return valueProperty.get();
	}

	public void setValue(T value) {
		valueProperty.set(value);
	}

	public ObjectProperty<T> valueProperty() {
		return valueProperty;
	}

}
