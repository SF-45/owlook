package space.sadfox.owlook;

import space.sadfox.owlook.utils.Logger;

public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Logger());

		App.go();

	}

}
