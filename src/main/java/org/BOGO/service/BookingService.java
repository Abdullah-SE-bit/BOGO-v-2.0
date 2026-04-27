package org.BOGO.service;

import org.BOGO.domain.booking.Booking;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Passenger;
import org.BOGO.repository.BookingRepository;
import org.BOGO.repository.BusRepository;
import java.util.List;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final BusRepository     busRepository;

    private Booking booking;


    public BookingService(BookingRepository bookingRepository, BusRepository busRepository) {
        this.bookingRepository = bookingRepository;
        this.busRepository     = busRepository;
    }

    /**
     * Creates, persists, and returns a confirmed Booking for the given passenger,
     * path, bus, and payment method. Also generates a QR code and updates the
     * driver's stop list.
     */
    public Booking createBooking(int PassengerID, int pStopID, int dStopID, String paymentMethod) {
        return null;
    }

    /**
     * Validates that the pickup and destination stops exist and are active.
     */
    public boolean validateStops(Stop pickup, Stop destination) {
        return false;
    }

    /**
     * Returns all buses operating toward the given destination from the pickup stop.
     */
    public List<Bus> getAvailableBuses(Stop pickup, Stop destination) {
        return null;
    }

    /**
     * Generates a unique, one-use QR code string tied to the given booking.
     */
    public String generateQRCode(Booking booking) {
        return null;
    }

    /**
     * Pushes the new pickup stop to the driver's live stop list for the given bus.
     */
    public void updateDriverStopList(Bus bus, Booking booking) {}

    /**
     * Returns all bookings currently in the system (for admin read).
     */
    public List<Booking> getAllBookings() {
        return null;
    }

    /**
     * Returns details of a single booking by ID.
     */
    public Booking getBookingByID(int bookingID) {
        return null;
    }
}
