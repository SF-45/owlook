package space.sadfox.owlook.utils;

public class ActionTimer {
	
	private Thread timer;
	private final Runnable action;
	private volatile int time;
	private volatile int delay;
	
	public ActionTimer(Runnable action) {
		this(action, 500);
	}

	public ActionTimer(Runnable action, int delay) {
		this.action = action;
		this.delay = delay;
	}
	
	public void run() {
		time = 1;
		if (timer == null || !timer.isAlive()) {
			timer = new Thread(() -> {
				for (;time <= this.delay; time++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						OwlLogger.registerException(2, e);
					}
				}
				action.run();
			});
			timer.start();
		}
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	
	
	
	

}
