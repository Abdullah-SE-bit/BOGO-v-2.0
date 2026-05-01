package org.BOGO.controller;

import org.BOGO.service.BookingService;
import org.BOGO.service.CancelationService;
import org.BOGO.service.PathBuildingService;

import org.BOGO.domain.booking.Booking;

public class BookingController {

    private final BookingService     bookingService = new BookingService();
    private final PathBuildingService     pathService = new PathBuildingService();
    private final CancelationService cancelationService = new CancelationService();

    public BookingController() {}

    public Booking BookBus(int passengerID, int srcID, int desID) {
        return bookingService.createBooking(passengerID, srcID, desID, pathService.buildPath(srcID, desID));
    }

    public boolean CancelBus(int bookingID, int passengerID) {
        return cancelationService.cancelBooking(bookingID, passengerID);
    }


}
