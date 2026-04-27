package org.BOGO.domain.communication;

import java.time.LocalDateTime;


public class Notification {


    private int notificationID;

    private String content;

    private boolean isRead;

    private LocalDateTime createdAt;

    private int recipientID;

    // ---------- Constructors ----------
    public Notification() {}

    //----------- Getters -----------------
    public boolean isRead(){ return isRead; }
}
