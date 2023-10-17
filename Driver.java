package nuber.students;

import java.util.Random;

public class Driver extends Person {

	private Passenger lastestPassengers;

	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger)
	{
		try{
			lastestPassengers = newPassenger;
			Random randomizedRandom = new Random();
			Thread.sleep(randomizedRandom.nextInt(maxSleep));
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() {
		try {
			Thread.sleep(lastestPassengers.getTravelTime());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
