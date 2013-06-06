package at.ac.uibk.akari.utils;


public class StopClock {
	
	private long stepSizeMS;
	
	public StopClock(final long stepSizeMS) {
		this.stepSizeMS=stepSizeMS;
	
		Thread colckThread=new Thread(new Runnable() {
			
			@Override
			public void run() {

			}
		});
		
	}
	
	
	
	
	public void start(){
		
	}
	
	public void stop(){
		
	}

}
