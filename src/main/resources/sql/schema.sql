/*
   SQL Server Table Creation Script
   Database: bogo
   Description: Bus Booking/Management System
*/

-- 1. PERSONAL_DETAILS (Dependency for USERS)
CREATE TABLE PERSONAL_DETAILS (
    PdId INT PRIMARY KEY IDENTITY(1,1),
    [Name] VARCHAR(30) NOT NULL,
    Email VARCHAR(100) NULL,
    [Password] VARCHAR(50) NULL,
    CNIC VARCHAR(13) NULL
);

-- 2. USERS (Dependency for ADMIN, PASSENGER, DRIVER)
CREATE TABLE USERS (
    UserId INT PRIMARY KEY IDENTITY(1,1),
    PdId INT NOT NULL,
    CONSTRAINT FK_USERS_PERSONAL_DETAILS FOREIGN KEY (PdId) 
        REFERENCES PERSONAL_DETAILS(PdId)
);

-- 3. ADMIN (Specialization of USERS)
CREATE TABLE ADMIN (
    UserId INT PRIMARY KEY,
    CONSTRAINT FK_ADMIN_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 4. PASSENGER (Specialization of USERS)
CREATE TABLE PASSENGER (
    UserId INT PRIMARY KEY,
    CONSTRAINT FK_PASSENGER_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 5. DRIVER (Specialization of USERS)
CREATE TABLE DRIVER (
    UserId INT PRIMARY KEY,
    DriverID CHAR(13) NOT NULL,
    CONSTRAINT FK_DRIVER_USERS FOREIGN KEY (UserId) 
        REFERENCES USERS(UserId)
);

-- 6. LOCATIONN (Dependency for STOPS)
CREATE TABLE LOCATIONN (
    LocationId INT PRIMARY KEY IDENTITY(1,1),
    Longitude FLOAT NOT NULL,
    Latitude FLOAT NOT NULL
);

-- 7. STOPS
CREATE TABLE STOPS (
    StopId INT PRIMARY KEY IDENTITY(1,1),
    StopName VARCHAR(50) NOT NULL,
    LocationId INT NOT NULL,
    Connections NVARCHAR(MAX) NOT NULL,
    CONSTRAINT FK_STOPS_LOCATIONN FOREIGN KEY (LocationId) 
        REFERENCES LOCATIONN(LocationId)
);

-- 8. ROUTEE
CREATE TABLE ROUTEE (
    RouteId INT PRIMARY KEY IDENTITY(1,1),
    Active BIT NOT NULL,
    Stop_IDs NVARCHAR(MAX) NOT NULL
);

-- 9. BUS
CREATE TABLE BUS (
    BusId INT PRIMARY KEY IDENTITY(1,1),
    BusCompany VARCHAR(50) NOT NULL,
    Registration VARCHAR(7) NULL,
    RegistrationYear INT NULL,
    BusStatus VARCHAR(20) NULL,
    Capacity INT NULL
);

-- 10. TRIP
CREATE TABLE TRIP (
    TripId INT PRIMARY KEY IDENTITY(1,1),
    RouteId INT NOT NULL,
    BusId INT NOT NULL,
    DriverId INT NOT NULL,
    DepartureTime DATETIME NOT NULL,
    ArrivalTime DATETIME NOT NULL,
    CONSTRAINT FK_TRIP_ROUTEE FOREIGN KEY (RouteId) REFERENCES ROUTEE(RouteId),
    CONSTRAINT FK_TRIP_BUS FOREIGN KEY (BusId) REFERENCES BUS(BusId),
    CONSTRAINT FK_TRIP_DRIVER FOREIGN KEY (DriverId) REFERENCES DRIVER(UserId)
);

-- 11. BOOKING
CREATE TABLE BOOKING (
    BookingID INT PRIMARY KEY IDENTITY(1,1),
    PassengerID INT NOT NULL,
    BusID INT NOT NULL,
    Active BIT NOT NULL,
    Cost FLOAT NOT NULL,
    BookingTime DATETIME NOT NULL,
    CONSTRAINT FK_BOOKING_PASSENGER FOREIGN KEY (PassengerID) REFERENCES PASSENGER(UserId),
    CONSTRAINT FK_BOOKING_BUS FOREIGN KEY (BusID) REFERENCES BUS(BusId)
);

CREATE TABLE ALERTS (
                        AlertId INT PRIMARY KEY IDENTITY(1,1),
                        SenderDriverId INT NOT NULL,
                        AlertType VARCHAR(20) NOT NULL, -- 'BUS_DOWN', 'DRIVER_DOWN', 'ROUTE_BLOCKAGE'
                        Priority VARCHAR(10) NOT NULL,  -- 'HIGH', 'LOW'
    [Message] NVARCHAR(MAX),
    Status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'RESOLVED'
    SentTime DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_ALERTS_DRIVER FOREIGN KEY (SenderDriverId) REFERENCES DRIVER(UserId)
    );

CREATE TABLE RESOLUTIONS (
                             ResolutionId INT PRIMARY KEY IDENTITY(1,1),
                             AlertId INT NOT NULL,
                             AdminId INT NOT NULL,
                             NewBusId INT NULL,
                             NewDriverId INT NULL,
                             NewRouteId INT NULL,
                             ResolutionNotes NVARCHAR(MAX),
                             ResolvedAt DATETIME DEFAULT GETDATE(),
                             CONSTRAINT FK_RES_ALERT FOREIGN KEY (AlertId) REFERENCES ALERTS(AlertId),
                             CONSTRAINT FK_RES_ADMIN FOREIGN KEY (AdminId) REFERENCES ADMIN(UserId),
                             CONSTRAINT FK_RES_BUS FOREIGN KEY (NewBusId) REFERENCES BUS(BusId),
                             CONSTRAINT FK_RES_DRIVER FOREIGN KEY (NewDriverId) REFERENCES DRIVER(UserId),
                             CONSTRAINT FK_RES_ROUTE FOREIGN KEY (NewRouteId) REFERENCES ROUTEE(RouteId)
);

CREATE TABLE NOTIFICATIONS (
                               NotificationId INT PRIMARY KEY IDENTITY(1,1),
                               RecipientUserId INT NOT NULL,
                               Content NVARCHAR(MAX) NOT NULL,
                               IsRead BIT DEFAULT 0,
                               CreatedAt DATETIME DEFAULT GETDATE(),
                               CONSTRAINT FK_NOTIF_USER FOREIGN KEY (RecipientUserId) REFERENCES USERS(UserId)
);