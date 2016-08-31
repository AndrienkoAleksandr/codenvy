/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.report;

import com.codenvy.api.license.server.license.CodenvyLicenseManager;
import com.codenvy.api.license.server.license.LicenseException;
import com.codenvy.report.shared.dto.Ip;
import com.codenvy.api.user.server.dao.AdminUserDao;
import com.codenvy.mail.MailSenderClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.schedule.ScheduleCron;

import javax.inject.Named;
import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends reports:
 * <li>to Codenvy with number of users.</li>
 *
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class ReportSender {
    private static final Logger LOG = Logger.getLogger(ReportSender.class.getSimpleName());

    private final MailSenderClient       mailClient;
    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 updateServerEndpoint;
    private final CodenvyLicenseManager  licenseManager;
    private final AdminUserDao           adminUserDao;
    private final String                 apiEndpoint;

    @Inject
    public ReportSender(@Named("report-sender.update_server_endpoint") String updateServerEndpoint,
                        @Named("api.endpoint") String apiEndpoint,
                        MailSenderClient mailClient,
                        HttpJsonRequestFactory httpJsonRequestFactory,
                        CodenvyLicenseManager licenseManager,
                        AdminUserDao adminUserDao) {
        this.mailClient = mailClient;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.updateServerEndpoint = updateServerEndpoint;
        this.licenseManager = licenseManager;
        this.adminUserDao = adminUserDao;
        this.apiEndpoint = apiEndpoint;
    }

    @ScheduleCron(cron = "0 0 1 ? * SUN *")  // send each Sunday at 1:00 AM. Use "0 0/1 * 1/1 * ? *" to send every 1 minute.
    public void sendWeeklyReports() {
        try {
            sendNumberOfUsers();
        } catch (JsonParseException | IOException | MessagingException | ApiException e) {
            LOG.log(Level.SEVERE, "Error of sending weekly reports.", e);
        }
    }

    private void sendNumberOfUsers() throws IOException, MessagingException, JsonParseException, ApiException {
        try {
            if (licenseManager.isCodenvyUsageLegal()) {
                // don't send report if Codenvy License is valid and isn't expired
                LOG.log(Level.INFO, "Codenvy License is valid and isn't expired.");
                return;
            }
        } catch (LicenseException e) {
            // just log info without interrupting sending report if there is a problem with Codenvy License
            LOG.log(Level.INFO, "There is a problem with Codenvy License.", e);
        }

        ReportParameters parameters = obtainReportParameters(ReportType.CODENVY_ONPREM_USER_NUMBER_REPORT);

        Ip externalIP = obtainExternalIP();

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("External IP address: %s\n", externalIP.getValue()));
        msg.append(String.format("Hostname: %s\n", new URL(apiEndpoint).getHost()));
        msg.append(String.format("Number of users: %s\n", adminUserDao.getAll(30, 0).getTotalItemsCount()));  // TODO ndp (codenvy/600) fix it get all users number

        mailClient.sendMail(parameters.getSender(), parameters.getReceiver(), null, parameters.getTitle(), MediaType.TEXT_PLAIN, msg.toString());
    }

    private Ip obtainExternalIP() throws IOException,
                                             ForbiddenException,
                                             BadRequestException,
                                             ConflictException,
                                             NotFoundException,
                                             ServerException,
                                             UnauthorizedException {
        String requestUrl = String.format("%s/util/client-ip", updateServerEndpoint);

        return httpJsonRequestFactory.fromUrl(requestUrl)
                                     .request()
                                     .asDto(Ip.class);

    }

    private ReportParameters obtainReportParameters(ReportType reportType) throws
                                                                           IOException,
                                                                           JsonParseException,
                                                                           ForbiddenException,
                                                                           BadRequestException,
                                                                           ConflictException,
                                                                           NotFoundException,
                                                                           ServerException,
                                                                           UnauthorizedException {
        String requestUrl = String.format("%s/report/parameters/%s",
                                          updateServerEndpoint,
                                          reportType.name().toLowerCase());

        return httpJsonRequestFactory.fromUrl(requestUrl)
                                     .request()
                                     .as(ReportParameters.class, ReportParameters.class.getGenericSuperclass());

    }
}
