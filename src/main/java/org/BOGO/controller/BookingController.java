package org.BOGO.controller;

import org.BOGO.service.BookingService;
import org.BOGO.service.CancelationService;

public class BookingController {

    private final BookingService     bookingService = new BookingService();
    private final CancelationService cancelationService = new CancelationService();

    public BookingController() {}

    public boolean BookBus(int passengerID, int busID, int paymentMethod) {

        return false;
    }


}
