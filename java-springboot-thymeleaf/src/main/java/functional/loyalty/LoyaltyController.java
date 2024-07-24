package com.yrnet.spark.front.functional.loyalty;

import com.yrnet.spark.front.external.hylo.exception.cart.CartException;
import com.yrnet.spark.front.functional.account.form.FormFacade;
import com.yrnet.spark.front.functional.commerce.bean.cms.Page;
import com.yrnet.spark.front.functional.commerce.cart.CartFacade;
import com.yrnet.spark.front.functional.constant.AttributesPage;
import com.yrnet.spark.front.functional.constant.GlobalConstants;
import com.yrnet.spark.front.functional.constant.PageConstant;
import com.yrnet.spark.front.functional.loyalty.exception.LoyaltyException;
import com.yrnet.spark.front.functional.loyalty.form.LoyaltyForm;
import com.yrnet.spark.front.functional.loyalty.service.LoyaltyCustomerInSessionService;
import com.yrnet.spark.front.technical.session.SessionLoyaltyHelper;
import com.yrnet.spark.loyaltyfacadeclient.dto.LoyaltyResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.yrnet.spark.front.functional.constant.AttributesPage.VOUCHER_ERROR;
import static com.yrnet.spark.front.functional.constant.PageConstant.REDIRECT_CART_PAGE;
import static com.yrnet.spark.front.functional.constant.SessionConstant.SHOW_POPUP_CONDITION_KEY;
import static com.yrnet.spark.front.util.exception.ExceptionHelper.CART_NOT_CREATED_YET;


@Slf4j
@Controller
public class LoyaltyController {

    private final LoyaltyPageHelper loyaltyPageHelper;
    private final LoyaltyFacade loyaltyFacade;
    private final FormFacade formFacade;
    private final CartFacade cartFacade;
    private final LoyaltyOffersFacade loyaltyOffersFacade;
    private final SessionLoyaltyHelper sessionLoyaltyHelper;
    private final LoyaltyCustomerInSessionService loyaltyCustomerInSessionService;

    @Autowired
    public LoyaltyController(LoyaltyPageHelper loyaltyPageHelper,
                             LoyaltyFacade loyaltyFacade,
                             FormFacade formFacade,
                             CartFacade cartFacade,
                             LoyaltyOffersFacade loyaltyOffersFacade,
                             SessionLoyaltyHelper sessionLoyaltyHelper,
                             LoyaltyCustomerInSessionService loyaltyCustomerInSessionService) {
        this.loyaltyPageHelper = loyaltyPageHelper;
        this.loyaltyFacade = loyaltyFacade;
        this.formFacade = formFacade;
        this.cartFacade = cartFacade;
        this.loyaltyOffersFacade = loyaltyOffersFacade;
        this.sessionLoyaltyHelper = sessionLoyaltyHelper;
        this.loyaltyCustomerInSessionService = loyaltyCustomerInSessionService;
    }

    @GetMapping("/loyalty-login")
    public String getLoyaltyLogin(Model model,
                                  @ModelAttribute("form") LoyaltyForm form,
                                  BindingResult errors) {

        loyaltyFacade.fillLoyaltyForm(form);

        if (loyaltyFacade.hasCredentialsIntoCookie()) {
            try {
                final LoyaltyResponseData loyaltyData = loyaltyFacade.getLoyaltyPoint(form);
                model.addAttribute(AttributesPage.PAGE, loyaltyPageHelper.getLoyaltyPage(loyaltyData, form));
                return PageConstant.DYNAMIC;
            } catch (LoyaltyException e) {
                log.error(e.getMessage(), e);
                errors.addError(new ObjectError(GlobalConstants.GLOBAL, "form.loyalty-login.error"));
            }
        }

        return getFormulaire(model);
    }

    @PostMapping("/loyalty-login")
    public String submit(HttpServletResponse response,
                         @Valid @ModelAttribute(AttributesPage.FORM) LoyaltyForm form,
                         BindingResult errors,
                         Model model) {

        if (errors.hasErrors()) {
            errors.addError(new ObjectError(GlobalConstants.GLOBAL, "form.loyalty-login.fields.error"));
            return getFormulaire(model);
        }

        try {
            final LoyaltyResponseData loyaltyData = loyaltyFacade.getLoyaltyPoint(form);
            model.addAttribute(AttributesPage.PAGE, loyaltyPageHelper.getLoyaltyPage(loyaltyData, form));
        } catch (LoyaltyException e) {
            log.error(e.getMessage(), e);
            errors.addError(new ObjectError(GlobalConstants.GLOBAL, "form.loyalty-login.error"));
            return getFormulaire(model);
        }

        // save in cookie loyalty credentials if customer has choose "remember me"
        loyaltyFacade.saveCredentials(response, form);

        return PageConstant.DYNAMIC;
    }

    private String getFormulaire(Model model) {

        // Get page
        final Page page = loyaltyPageHelper.getLoyaltyLogin();

        // override form component and inject form if needed
        // TODO: Remove when delivery redesign will be merged
        formFacade.addFormToModel(model, page);

        // Fill model
        model.addAttribute(AttributesPage.PAGE, page);

        return PageConstant.DYNAMIC;
    }

}
