package org.BOGO.domain.booking;

import org.BOGO.domain.transport.Bus;
import org.BOGO.domain.user.Passenger;
import java.time.LocalDateTime;

public class Booking {

    private int bookingID;
    private boolean active;

    private Path path;
    private Passenger passenger;
    private Bus bus;
    private double cost;
    private String status;
    private String paymentMethod;  // EWALLET | CASH
    private String qrCode;

    private LocalDateTime createdAt;



    // ---------- Business Methods ----------
    /** Marks this booking as COMPLETED and deactivates it. */
    public void confirm() {}

    /** Marks this booking as CANCELLED and deactivates it. */
    public void cancel() {}

    /** Generates and assigns a unique QR code string for this booking. */
    public void generateQrCode() {}

    // Getters
    public int             getBookingID()     { return bookingID; }
    public boolean         isActive()         { return active; }
    public Path            getPath()          { return path; }
    public Passenger       getPassenger()     { return passenger; }
    public Bus             getBus()           { return bus; }
    public double          getCost()          { return cost; }
    public String          getStatus()        { return status; }
    public String          getPaymentMethod() { return paymentMethod; }
    public String          getQrCode()        { return qrCode; }
    public LocalDateTime   getCreatedAt()     { return createdAt; }

    // Setters
    public void setBookingID(int bookingID)           { this.bookingID     = bookingID; }
    public void setActive(boolean active)             { this.active        = active; }
    public void setPath(Path path)                    { this.path          = path; }
    public void setPassenger(Passenger passenger)     { this.passenger     = passenger; }
    public void setBus(Bus bus)                       { this.bus           = bus; }
    public void setCost(double cost)                  { this.cost          = cost; }
    public void setPaymentMethod(String method)       { this.paymentMethod = method; }
    public void setQrCode(String qrCode)              { this.qrCode        = qrCode; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt     = createdAt; }
    public void setStatus(String status)              { this.status        = status; }
}
