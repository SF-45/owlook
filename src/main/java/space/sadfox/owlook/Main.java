package space.sadfox.owlook;


import space.sadfox.owlook.utils.OwlLogger;

public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new OwlLogger());

		App.go();
		
		
		
		
		
	}

}
