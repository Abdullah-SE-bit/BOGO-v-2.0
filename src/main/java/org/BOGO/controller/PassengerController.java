package org.BOGO.controller;

import org.BOGO.service.AuthService;
import org.BOGO.service.BookingService;


public class PassengerController {

    private final BookingService  bookingService;
    private final AuthService     authService;

    public PassengerController(BookingService bookingService,
                               AuthService authService) {
        this.bookingService  = bookingService;
        this.authService     = authService;
    }

}
