package org.BOGO.controller;

import org.BOGO.service.AuthService;
import org.BOGO.service.BookingService;

public class UserController {

    private final BookingService  bookingService = new BookingService();
    private final AuthService     authService = new AuthService();

}
