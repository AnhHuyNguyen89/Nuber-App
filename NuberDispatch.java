package nuber.students;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	private int pendingBooking;	//The number of pending bookings that are waiting for a driver
	private boolean logEvents;
	public int bookingID;	//The booking ID generated for each booking, must be sequential
	private HashMap<String, Integer> regionInfo;
	private BlockingQueue<Driver> driverWaitingQueue;	//The blocking queue that is thread-safe, used to safely add and remove elements in multithreaded program
	private HashMap<String, NuberRegion> regions;
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		System.out.println("Start to create a Nuber Dispatch");
		this.regionInfo = regionInfo;
		this.logEvents = logEvents;
		driverWaitingQueue = new ArrayBlockingQueue<Driver>(MAX_DRIVERS);
		pendingBooking = 0;
		bookingID = 1;
		
		//Create regions and add them right here
		regions = new HashMap<String, NuberRegion>();
		System.out.println("Creating " + this.regionInfo.size() + " regions");
		for(var entry: this.regionInfo.entrySet()) {
			String regionName = entry.getKey();
			Integer maxBooking = entry.getValue();
			NuberRegion newRegion = new NuberRegion(this, regionName, maxBooking);
			regions.put(regionName, newRegion);
		}
		System.out.println("Has done the creating " + this.regionInfo.size() + " regions");
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public boolean addDriver(Driver newDriver)
	{
		try {
			driverWaitingQueue.put(newDriver);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public Driver getDriver()
	{
		try {
			Driver valDriver = driverWaitingQueue.take();
			pendingBooking--;
			return valDriver;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		if (!this.logEvents) return;
		System.out.println(booking + "has: " + message);
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion bookingRegion = regions.get(region);
		Future<BookingResult> res = bookingRegion.bookPassenger(passenger);
		bookingID++;
		if(res != null) pendingBooking++;
		return res;
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver()
	{
		return pendingBooking;
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
		for(NuberRegion region: regions.values()) {
			region.shutdown();
		}
	}

}
