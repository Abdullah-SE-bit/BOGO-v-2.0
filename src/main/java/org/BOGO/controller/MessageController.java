package org.BOGO.controller;

import org.BOGO.service.HandleDriverService;
import org.BOGO.service.IssueReportingService;
import org.BOGO.service.IssueResolvingService;
import org.BOGO.service.NotificationService;

public class MessageController {

    private final IssueReportingService  issueReportingService = new IssueReportingService();
    private final IssueResolvingService  issueResolvingService = new IssueResolvingService();
    private final NotificationService    notificationService = new NotificationService();
    private final HandleDriverService    handleDriverService = new HandleDriverService();



}
