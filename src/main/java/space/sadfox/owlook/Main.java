package space.sadfox.owlook;

import space.sadfox.owlook.utils.Owlook;

public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Owlook());

		App.go();

	}

}
