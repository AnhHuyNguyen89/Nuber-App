package nuber.students;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 
 * Booking represents the overall "job" for a passenger getting to their destination.
 * 
 * It begins with a passenger, and when the booking is commenced by the region 
 * responsible for it, an available driver is allocated from dispatch. If no driver is 
 * available, the booking must wait until one is. When the passenger arrives at the destination,
 * a BookingResult object is provided with the overall information for the booking.
 * 
 * The Booking must track how long it takes, from the instant it is created, to when the 
 * passenger arrives at their destination. This should be done using Date class' getTime().
 * 
 * Booking's should have a globally unique, sequential ID, allocated on their creation. 
 * This should be multi-thread friendly, allowing bookings to be created from different threads.
 * 
 * @author james
 *
 */
public class Booking implements Callable<BookingResult>{

	private NuberDispatch dispatch;
	private Passenger passenger;
	private BookingResult resultOfBook;
	
	/**
	 * Creates a new booking for a given Nuber dispatch and passenger, noting that no
	 * driver is provided as it will depend on whether one is available when the region 
	 * can begin processing this booking.
	 * 
	 * @param dispatch
	 * @param passenger
	 */
	public Booking(NuberDispatch dispatch, Passenger passenger)
	{
		this.dispatch = dispatch;
		this.passenger = passenger;
		resultOfBook = new BookingResult(dispatch.bookingID, null, null, 0);
		dispatch.logEvent(this, "Booking has been created");
	}
	
	/**
	 * At some point, the Nuber Region responsible for the booking can start it (has free spot),
	 * and calls the Booking.call() function, which:
	 * 1.	Asks Dispatch for an available driver
	 * 2.	If no driver is currently available, the booking must wait until one is available. 
	 * 3.	Once it has a driver, it must call the Driver.pickUpPassenger() function, with the 
	 * 			thread pausing whilst as function is called.
	 * 4.	It must then call the Driver.driveToDestination() function, with the thread pausing 
	 * 			whilst as function is called.
	 * 5.	Once at the destination, the time is recorded, so we know the total trip duration. 
	 * 6.	The driver, now free, is added back into Dispatch�s list of available drivers. 
	 * 7.	The call() function the returns a BookingResult object, passing in the appropriate 
	 * 			information required in the BookingResult constructor.
	 *
	 * @return A BookingResult containing the final information about the booking 
	 */

	public BookingResult call() {
		resultOfBook.passenger = passenger;
		dispatch.logEvent(this, "Starting to make a booking, connecting to driver");
		resultOfBook.driver = dispatch.getDriver();
		
		//Pick up passenger and drive
		long startDate = new Date().getTime();
		dispatch.logEvent(this, "Starting a trip now, it's on way to a passenger");
		resultOfBook.driver.pickUpPassenger(passenger);
		
		dispatch.logEvent(this, "Correctly picked up passenger, driving to destination");
		resultOfBook.driver.driveToDestination();
		long endDate = new Date().getTime();
		resultOfBook.tripDuration = endDate - startDate;
		
		dispatch.logEvent(this, "Passenger has arrived at destination, and a driver is now ready for next trip");
		dispatch.addDriver(resultOfBook.driver);
		return resultOfBook;
	}
	
	/***
	 * Should return the:
	 * - booking ID, 
	 * - followed by a colon, 
	 * - followed by the driver's name (if the driver is null, it should show the word "null")
	 * - followed by a colon, 
	 * - followed by the passenger's name (if the passenger is null, it should show the word "null")
	 * 
	 * @return The compiled string
	 */
	
	@Override
	public String toString()
	{
		return String.format("%d: %s: %s: ", 
			resultOfBook.jobID, 
			resultOfBook.driver != null ? resultOfBook.driver.name : "null", 
			resultOfBook.passenger != null ? resultOfBook.passenger.name : "null"
		);
	}

}
