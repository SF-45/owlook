package space.sadfox.owlook.jaxb.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StringPropertyAdapter extends XmlAdapter<String, StringProperty> {

	@Override
	public StringProperty unmarshal(String v) throws Exception {
		return new SimpleStringProperty(v);
	}

	@Override
	public String marshal(StringProperty v) throws Exception {
		return v.get();
	}

}
