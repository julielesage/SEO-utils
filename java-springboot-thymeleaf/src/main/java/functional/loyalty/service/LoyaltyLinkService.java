package functional.loyalty.service;

import functional.constant.AttributesPage;
import functional.constant.GlobalConstants;
import functional.locality.service.LocalityService;
import functional.loyalty.exception.LoyaltyAlreadyAttachedException;
import functional.loyalty.exception.LoyaltyNoDataFetchedException;
import functional.loyalty.exception.LoyaltySpaceAttachException;
import functional.loyalty.exception.LoyaltySpaceException;
import functional.loyalty.form.LoyaltyAttachForm;
import functional.loyalty.space.LoyaltySpaceFacade;
import technical.message.MessageHelper;
import util.tracking.TrackingHelper;
import util.tracking.loyalty.LoyaltyTracking;
import util.tracking.loyalty.LoyaltyTrackingAction;
import util.tracking.loyalty.LoyaltyTrackingLabel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import static util.tracking.loyalty.LoyaltyTracking.TRACKING_EVENT;

@Slf4j
@Service
public class LoyaltySpaceAttachService {

    private static final String LOYALTY_CARD_NUMBER_FIELD = "loyaltycardnumber";

    private static final String POSTAL_CODE_FIELD = "postalcode";

    private static final String FORM_LOYALTY_ATTACH_INVALID_POSTALCODE_ERROR = "form.loyalty-attach.invalid-postalcode.error";

    private static final String FORM_VALIDATION_LOYALTY_INTERNAL_SERVER_ERROR = "form.validation.loyalty.internal-server-error";

    private static final String FORM_LOYALTY_ATTACH_ALREADY_ATTACHED_ERROR = "form.loyalty-attach.already-attached.error";

    private static final String FORM_LOYALTY_ATTACH_INVALID_CARD_ERROR = "form.loyalty-attach.invalid-card.error";

    private final LoyaltySpaceFacade loyaltySpaceFacade;

    private final LoyaltyAttachService loyaltyAttachService;

    private final LocalityService localityService;

    private final MessageHelper messageHelper;

    private final TrackingHelper trackingHelper;

    public LoyaltySpaceAttachService(LoyaltySpaceFacade loyaltySpaceFacade,
                                     LoyaltyAttachService loyaltyAttachService,
                                     LocalityService localityService,
                                     MessageHelper messageHelper,
                                     TrackingHelper trackingHelper) {
        this.loyaltySpaceFacade = loyaltySpaceFacade;
        this.loyaltyAttachService = loyaltyAttachService;
        this.localityService = localityService;
        this.messageHelper = messageHelper;
        this.trackingHelper = trackingHelper;
    }


    public void postLoyaltyAttach(LoyaltyAttachForm form,
                                  BindingResult errors,
                                  Model model) {

        if (errors.hasErrors()) {
            log.debug("Errors at loyalty attach validation : {}", errors.getAllErrors());
            throw new LoyaltySpaceAttachException("Errors at loyalty attach validation : " + errors.getAllErrors());
        }

        if (postalCodeIsPresentAndInvalid(form)) {

            addAttributesInModelForInvalidPostalCodeError(errors, model);

            throw new LoyaltySpaceAttachException("loyalty cart attach not possible, locality not found with postal code : " + form
                    .getPostalcode());
        }

        try {
            loyaltyAttachService.attachLoyaltyCardToAccount(form);

            addAttributesInModelForSuccess(model);

        } catch (LoyaltyNoDataFetchedException exception) {

            addAttributesInModelForLoyaltyNoDataFetchedError(errors, model);

            throw new LoyaltySpaceAttachException(exception.getMessage());

        } catch (LoyaltyAlreadyAttachedException exception) {

            addAttributesInModelForLoyaltyAlreadyAttachedError(errors, model, exception.getEmailAlreadyAttached());

            throw new LoyaltySpaceAttachException(exception.getMessage());

        } catch (LoyaltySpaceException exception) {

            addAttributesInModelForLoyaltySpaceError(errors, model);

            throw new LoyaltySpaceAttachException(exception.getMessage());
        }

    }

    private void addAttributesInModelForInvalidPostalCodeError(BindingResult errors, Model model) {
        errors.addError(new FieldError(AttributesPage.FORM, POSTAL_CODE_FIELD,
                FORM_LOYALTY_ATTACH_INVALID_POSTALCODE_ERROR));

        addTrackingErrorToModel(model, LoyaltyTrackingLabel.INVALID_POSTAL_CODE);
    }

    private void addAttributesInModelForLoyaltySpaceError(BindingResult errors, Model model) {
        errors.addError(new ObjectError(GlobalConstants.GLOBAL, FORM_VALIDATION_LOYALTY_INTERNAL_SERVER_ERROR));

        addTrackingErrorToModel(model, LoyaltyTrackingLabel.INTERNAL_SERVER_ERROR);
    }

    private void addAttributesInModelForLoyaltyAlreadyAttachedError(BindingResult errors, Model model, String email) {
        errors.addError(new FieldError(AttributesPage.FORM, LOYALTY_CARD_NUMBER_FIELD,
                messageHelper.getMessage(FORM_LOYALTY_ATTACH_ALREADY_ATTACHED_ERROR, email)));

        addTrackingErrorToModel(model, LoyaltyTrackingLabel.CARD_ALREADY_ATTACHED);
    }

    private void addAttributesInModelForLoyaltyNoDataFetchedError(BindingResult errors, Model model) {
        errors.addError(new FieldError(AttributesPage.FORM, LOYALTY_CARD_NUMBER_FIELD,
                FORM_LOYALTY_ATTACH_INVALID_CARD_ERROR));

        addTrackingErrorToModel(model, LoyaltyTrackingLabel.INVALID_CARD);
    }

    private void addAttributesInModelForSuccess(Model model) {
        model.addAttribute(TRACKING_EVENT, trackingHelper
                .getTrackingEvent(LoyaltyTracking.withAction(LoyaltyTrackingAction.ACCOUNT, LoyaltyTrackingLabel.ASSOCIATE_FID_CARD).getEvent()));

        model.addAttribute(AttributesPage.PAGE, loyaltySpaceFacade.getLoyaltySpacePage());
    }

    private boolean postalCodeIsPresentAndInvalid(LoyaltyAttachForm form) {
        return StringUtils.isNotEmpty(form.getPostalcode()) && CollectionUtils
                .isEmpty(localityService.findLocality(form.getPostalcode()));
    }

    private void addTrackingErrorToModel(Model model, LoyaltyTrackingLabel label) {
        model.addAttribute(TRACKING_EVENT, trackingHelper
                .getTrackingEvent(LoyaltyTracking.withAction(LoyaltyTrackingAction.ACCOUNT, label).getEvent()));
    }

}
