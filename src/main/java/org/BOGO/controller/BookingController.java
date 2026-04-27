package org.BOGO.controller;

import org.BOGO.service.BookingService;
import org.BOGO.service.CancelationService;

public class BookingController {

    private final BookingService     bookingService;
    private final CancelationService cancelationService;

    public BookingController(BookingService bookingService,
                             CancelationService cancelationService) {
        this.bookingService     = bookingService;
        this.cancelationService = cancelationService;
    }

    /**
     * POST /api/bookings
     * Books a ride for the passenger from pickup to destination stop.
     */

}
