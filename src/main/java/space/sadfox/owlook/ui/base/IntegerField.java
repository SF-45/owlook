package space.sadfox.owlook.ui.base;

import javafx.util.StringConverter;

public class IntegerField extends NumberField<Integer> {

	public IntegerField() {
		super(0);
	}

	public IntegerField(Integer initValue, Integer minValue, Integer maxValue) {
		super(initValue, minValue, maxValue);
	}

	public IntegerField(Integer initValue) {
		super(initValue);
	}

	@Override
	protected StringConverter<Integer> stringConverter() {
		return new StringConverter<Integer>() {
			@Override
			public String toString(Integer object) {
				return String.valueOf(object);
			}

			@Override
			public Integer fromString(String string) {
				return normolize(Integer.parseInt(string));

			}
		};
	}

	@Override
	protected Boolean inputTextFilter(String newInput) {
		return newInput.matches("[0-9]+");
	}

	@Override
	protected Boolean rezultTextFilter(String newText) {
		return true;
	}

}