package space.sadfox.owlook;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import space.sadfox.owlook.jaxb.ChangeHistory;
import space.sadfox.owlook.utils.ErrorLogger;

public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ErrorLogger());

		try {

			//printData();
			//createTableDataFilter();
			//createTableDataView();

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringProperty stringProperty = new SimpleStringProperty("start");
		IntegerProperty integerProperty = new SimpleIntegerProperty(0);
		ChangeHistory listener = new ChangeHistory();
		listener.register(stringProperty);
		listener.register(integerProperty);
		stringProperty.set("Sergey");
		stringProperty.set("Anthon");
		integerProperty.set(10);
		stringProperty.set("Anvar");
		integerProperty.set(15);
		
		System.out.println("Before change: " + stringProperty.get() + " " + integerProperty.get());
		listener.back();
		System.out.println("1 back: " + stringProperty.get() + " " + integerProperty.get());
		listener.back();
		System.out.println("2 back: " + stringProperty.get() + " " + integerProperty.get());
		listener.back();
		System.out.println("3 back: " + stringProperty.get() + " " + integerProperty.get());
		listener.back();
		System.out.println("4 back: " + stringProperty.get() + " " + integerProperty.get());
		listener.back();
		System.out.println("5 back: " + stringProperty.get() + " " + integerProperty.get());
		
		listener.forward();
		System.out.println("1 forward: " + stringProperty.get() + " " + integerProperty.get());
		listener.forward();
		System.out.println("2 forward: " + stringProperty.get() + " " + integerProperty.get());
		listener.forward();
		System.out.println("3 forward: " + stringProperty.get() + " " + integerProperty.get());
		listener.forward();
		System.out.println("4 forward: " + stringProperty.get() + " " + integerProperty.get());
		listener.forward();
		System.out.println("5 forward: " + stringProperty.get() + " " + integerProperty.get());
		
		ObservableList<String> list = FXCollections.observableArrayList();
		ChangeHistory listenerList = new ChangeHistory();
		listenerList.register(list);
		list.add("Sergey");
		list.add("Anton");
		list.remove("Sergey");
		list.addAll("Anvar", "Aniya");
		list.add("Nika");
		list.add("Maxim");
		
		System.out.println("Before change: " + list);
		listenerList.back();
		System.out.println("1 back: " + list);
		listenerList.back();
		System.out.println("2 back: " + list);
		listenerList.back();
		System.out.println("3 back: " + list);
		listenerList.back();
		System.out.println("4 back: " + list);
		listenerList.back();
		System.out.println("5 back: " + list);
		listenerList.back();
		System.out.println("6 back: " + list);
		listenerList.back();
		System.out.println("7 back: " + list);
		
		listenerList.forward();
		System.out.println("1 forward: " + list);
		listenerList.forward();
		System.out.println("2 forward: " + list);
		listenerList.forward();
		System.out.println("3 forward: " + list);
		listenerList.forward();
		System.out.println("4 forward: " + list);
		listenerList.forward();
		System.out.println("5 forward: " + list);
		listenerList.forward();
		System.out.println("6 forward: " + list);
		listenerList.forward();
		System.out.println("7 forward: " + list);
		
		App.go();
		
		
		
		
		
	}

}
