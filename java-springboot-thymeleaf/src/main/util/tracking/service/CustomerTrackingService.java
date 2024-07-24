package util.tracking.service;

import external.account.model.AuthenticationType;
import external.account.model.CustomerConnection;
import technical.session.SessionDatalayerHelper;
import technical.session.SessionUserHelper;
import util.tracking.model.CustomerTrackingEvent;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CustomerTrackingService {

    private static final String SIGNUP = "signup";

    private static final String SEPARATOR = "-";

    private static final String USER = "user";

    private final SessionDatalayerHelper sessionDatalayerHelper;

    private final SessionUserHelper sessionUserHelper;

    public CustomerTrackingService(SessionDatalayerHelper sessionDatalayerHelper, SessionUserHelper sessionUserHelper) {
        this.sessionDatalayerHelper = sessionDatalayerHelper;
        this.sessionUserHelper = sessionUserHelper;
    }

    public CustomerTrackingEvent getCustomerConnectionEvent() {

        if (datalayerConnectionEventNotPushed()) {
            return sessionUserHelper.getLastCustomerConnection()
                    .map(customerConnection -> {
                        sessionDatalayerHelper.setDatalayerConnectionEventPushed();
                        return buildCustomerTrackingEvent(customerConnection);
                    })
                    .orElse(null);
        }
        return null;
    }

    private boolean datalayerConnectionEventNotPushed() {
        return !sessionDatalayerHelper.isDatalayerConnectionEventPushed();
    }

    private static CustomerTrackingEvent buildCustomerTrackingEvent(CustomerConnection lastConnection) {
        return CustomerTrackingEvent.builder()
                .event(buildEvent(lastConnection))
                .method(buildMethod(lastConnection))
                .build();
    }

    private static String buildEvent(CustomerConnection lastConnection) {
        return USER + SEPARATOR + buildType(lastConnection) + SEPARATOR + buildStatus(lastConnection);
    }

    private static String buildMethod(CustomerConnection lastConnection) {
        return lastConnection.getOrigin().name();
    }

    private static String buildType(CustomerConnection lastConnection) {
        if (AuthenticationType.REGISTER == lastConnection.getType()) {
            return SIGNUP;
        }
        return StringUtils.lowerCase(lastConnection.getType().name());
    }

    private static String buildStatus(CustomerConnection lastConnection) {
        return StringUtils.lowerCase(lastConnection.getStatus().name());
    }

}
