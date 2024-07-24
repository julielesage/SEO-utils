package com.yrnet.spark.front.util.tracking.loyalty;


import com.yrnet.spark.front.util.tracking.model.TrackingEvent;

import java.util.Optional;

public final class LoyaltyTracking {

    public static final String TRACKING_EVENT = "trackingEvent";

    public static final String EVENT = "event";

    public static final String ENGAGEMENT = "engagement";

    public static final String FID_CARD = "fidCard";

    private final TrackingEvent trackingEvent;

    private LoyaltyTracking(TrackingEvent trackingEvent) {
        this.trackingEvent = trackingEvent;
    }

    public static LoyaltyTracking withAction(LoyaltyTrackingAction loyaltyTrackingAction, LoyaltyTrackingLabel loyaltyTrackingLabel) {
        return new LoyaltyTracking(buildTrackingEvent(loyaltyTrackingAction, loyaltyTrackingLabel));
    }

    public TrackingEvent getEvent() {
        return this.trackingEvent;
    }

    private static TrackingEvent buildTrackingEvent(LoyaltyTrackingAction loyaltyTrackingAction, LoyaltyTrackingLabel loyaltyTrackingLabel) {
        return TrackingEvent.builder()
                .event(EVENT)
                .category(ENGAGEMENT)
                .label(Optional.ofNullable(loyaltyTrackingLabel).map(LoyaltyTrackingLabel::getValue).orElse(""))
                .action(loyaltyTrackingAction.getValue())
                .build();
    }
}
