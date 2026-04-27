package org.BOGO.controller;

import org.BOGO.service.HandleDriverService;
import org.BOGO.service.IssueReportingService;
import org.BOGO.service.IssueResolvingService;
import org.BOGO.service.NotificationService;

public class MessageController {

    private final IssueReportingService  issueReportingService;
    private final IssueResolvingService  issueResolvingService;
    private final NotificationService    notificationService;
    private final HandleDriverService    handleDriverService;

    public MessageController(IssueReportingService issueReportingService,
                             IssueResolvingService issueResolvingService,
                             NotificationService notificationService,
                             HandleDriverService handleDriverService) {
        this.issueReportingService = issueReportingService;
        this.issueResolvingService = issueResolvingService;
        this.notificationService   = notificationService;
        this.handleDriverService   = handleDriverService;
    }


}
