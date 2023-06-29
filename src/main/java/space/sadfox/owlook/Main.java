package space.sadfox.owlook;


import space.sadfox.owlook.utils.ErrorLogger;

public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new ErrorLogger());

		App.go();
		
		
		
		
		
	}

}
