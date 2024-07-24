package com.yrnet.spark.front.util.tracking;

import com.yrnet.spark.commons.helpers.ConvertJsonHelper;
import com.yrnet.spark.commons.properties.CommonsProperties;
import com.yrnet.spark.digitalaccountclient.dto.customer.CustomerDTO;
import com.yrnet.spark.front.functional.commerce.bean.cms.ContentSlot;
import com.yrnet.spark.front.functional.commerce.bean.cms.Page;
import com.yrnet.spark.front.functional.commerce.bean.cms.components.SummaryComponent;
import com.yrnet.spark.front.functional.constant.FeatureConstant;
import com.yrnet.spark.front.functional.cookie.CookieService;
import com.yrnet.spark.front.functional.helper.SlotHelper;
import com.yrnet.spark.front.functional.loyalty.components.LoyaltyAdvantageComponent;
import com.yrnet.spark.front.technical.device.DeviceHelper;
import com.yrnet.spark.front.technical.properties.EndPointsProperties;
import com.yrnet.spark.front.technical.session.SessionAuthenticationHelper;
import com.yrnet.spark.front.technical.session.SessionCampaignHelper;
import com.yrnet.spark.front.technical.session.SessionCartHelper;
import com.yrnet.spark.front.technical.session.SessionTrackingHelper;
import com.yrnet.spark.front.technical.session.SessionUrlHelper;
import com.yrnet.spark.front.technical.session.SessionUserHelper;
import com.yrnet.spark.front.util.SiteHelper;
import com.yrnet.spark.front.util.UtmDatalayerHelper;
import com.yrnet.spark.front.util.data.ProfileData;
import com.yrnet.spark.front.util.tracking.mapper.CustomerForTrackingMapper;
import com.yrnet.spark.front.util.tracking.model.CartTracking;
import com.yrnet.spark.front.util.tracking.model.CustomerTrackingEvent;
import com.yrnet.spark.front.util.tracking.model.OrderTracking;
import com.yrnet.spark.front.util.tracking.model.TrackingEvent;
import com.yrnet.spark.front.util.tracking.model.loyalty.RewardTracking;
import com.yrnet.spark.front.util.tracking.service.CustomerTrackingService;
import com.yrnet.spark.front.util.tracking.service.LoyaltyConfirmPurchaseTrackingService;
import com.yrnet.spark.front.util.tracking.service.LoyaltyTrackingService;
import com.yrnet.spark.front.util.tracking.service.SelectedAdvantageTrackingService;
import com.yrnet.spark.hybrisclient.model.CartWsDTO;
import com.yrnet.spark.hybrisclient.model.OrderWsDTO;
import com.yrnet.spark.hybrisclient.model.VoucherWsDTO;
import com.yrnet.spark.hybrisclient.model.custom.CmsComponent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class TrackingHelper {

    private final SelectedAdvantageTrackingService selectedAdvantageTrackingService;
    private final CustomerTrackingService customerTrackingService;
    private final HttpServletRequest request;
    private final CustomerForTrackingMapper customerForTrackingMapper;
    private final DeviceHelper deviceHelper;
    private final CommonsProperties commonsProperties;
    private final ConvertJsonHelper convertJsonHelper;
    private final SlotHelper slotHelper;
    private final SessionCampaignHelper sessionCampaignHelper;
    private final SessionCartHelper sessionCartHelper;
    private final SessionUserHelper sessionUserHelper;
    private final SessionAuthenticationHelper sessionAuthenticationHelper;
    private final SessionTrackingHelper sessionTrackingHelper;
    private final SiteHelper siteHelper;
    private final EndPointsProperties endPointsProperties;
    private final LoyaltyConfirmPurchaseTrackingService loyaltyConfirmPurchaseTrackingService;
    private final LoyaltyTrackingService loyaltyTrackingService;
    private final CookieService cookieService;
    private final SessionUrlHelper sessionUrlHelper;
    private final EncryptionEmailHelper encryptionEmailHelper;
    private final UtmDatalayerHelper utmDatalayerHelper;

    public TrackingHelper(SelectedAdvantageTrackingService selectedAdvantageTrackingService,
                          CustomerTrackingService customerTrackingService,
                          HttpServletRequest request,
                          CustomerForTrackingMapper customerForTrackingMapper,
                          DeviceHelper deviceHelper,
                          CommonsProperties commonsProperties,
                          ConvertJsonHelper convertJsonHelper,
                          SlotHelper slotHelper,
                          SessionCampaignHelper sessionCampaignHelper,
                          SessionCartHelper sessionCartHelper,
                          SessionUserHelper sessionUserHelper,
                          SessionAuthenticationHelper sessionAuthenticationHelper,
                          SessionTrackingHelper sessionTrackingHelper,
                          SiteHelper siteHelper,
                          EndPointsProperties endPointsProperties,
                          LoyaltyConfirmPurchaseTrackingService loyaltyConfirmPurchaseTrackingService,
                          LoyaltyTrackingService loyaltyTrackingService,
                          CookieService cookieService,
                          SessionUrlHelper sessionUrlHelper,
                          EncryptionEmailHelper encryptionEmailHelper,
                          UtmDatalayerHelper utmDatalayerHelper) {
        this.selectedAdvantageTrackingService = selectedAdvantageTrackingService;
        this.customerTrackingService = customerTrackingService;
        this.request = request;
        this.customerForTrackingMapper = customerForTrackingMapper;
        this.deviceHelper = deviceHelper;
        this.commonsProperties = commonsProperties;
        this.convertJsonHelper = convertJsonHelper;
        this.slotHelper = slotHelper;
        this.sessionCampaignHelper = sessionCampaignHelper;
        this.sessionCartHelper = sessionCartHelper;
        this.sessionUserHelper = sessionUserHelper;
        this.sessionAuthenticationHelper = sessionAuthenticationHelper;
        this.sessionTrackingHelper = sessionTrackingHelper;
        this.siteHelper = siteHelper;
        this.endPointsProperties = endPointsProperties;
        this.loyaltyConfirmPurchaseTrackingService = loyaltyConfirmPurchaseTrackingService;
        this.loyaltyTrackingService = loyaltyTrackingService;
        this.cookieService = cookieService;
        this.sessionUrlHelper = sessionUrlHelper;
        this.encryptionEmailHelper = encryptionEmailHelper;
        this.utmDatalayerHelper = utmDatalayerHelper;
    }


    public boolean isEnable() {
        return siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE)
                && !sessionTrackingHelper.hasDisableTracking();
    }

    /**
     * @return different gtm options depends on one trust activation
     */
    public String gtmOptions() {
        if (siteHelper.isfeatureActive(FeatureConstant.ONE_TRUST_ENABLE)) {
            return endPointsProperties.getGtmOptions();
        }
        return endPointsProperties.getGtmOptionsWithoutOneTrust();
    }

    /**
     * @return customer connection event as string
     */
    public Optional<CustomerTrackingEvent> getCustomerConnectionEvent() {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE)) {
            return Optional.empty();
        }
        return Optional.ofNullable(customerTrackingService.getCustomerConnectionEvent());
    }

    /**
     * used by widget like submit cta
     *
     * @param trackingEvent tracking event
     * @return tracking event as string
     */
    public String getTrackingEvent(TrackingEvent trackingEvent) {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE)) {
            return null;
        }
        return convertJsonHelper.toJson(trackingEvent);
    }

    /**
     * @return tracking event for selected advantage
     */
    public String getSelectedAdvantageEvent(LoyaltyAdvantageComponent advantage) {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE)) {
            return StringUtils.EMPTY;
        }
        return convertJsonHelper.toJson(selectedAdvantageTrackingService.getSelectedAdvantageEvent(advantage));
    }

    public String getDataLayer(Page page) {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE)) {
            return StringUtils.EMPTY;
        }

        final DataLayer dataLayer = initDataLayer(page);
        dataLayer.setEvent("initialization");
        try {
            return convertJsonHelper.toJson(dataLayer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    public String refreshDataLayer(Page page) {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE) || page == null) {
            return StringUtils.EMPTY;
        }

        final DataLayer dataLayer = initDataLayer(page);
        dataLayer.setEvent("refresh");
        return convertJsonHelper.toJson(dataLayer);
    }

    public String dynamicDataLayer(Page page) {
        if (!siteHelper.isfeatureActive(FeatureConstant.TRACKING_ENABLE) || page == null) {
            return StringUtils.EMPTY;
        }

        final DataLayer dataLayer = initDataLayer(page);
        dataLayer.setEvent("virtualPageView");
        return convertJsonHelper.toJson(dataLayer);
    }

    private DataLayer initDataLayer(Page page) {
        return DataLayer.builder()
                .profile(commonsProperties.getEnvironment())
                .meta(page.getMeta())
                .label(page.getName())
                .components(componentAsMap(page))
                .cart(getEnrichedCartForTracking().orElse(null))
                .mobile(deviceHelper.isMobile())
                .site(siteHelper.getRealmCode())
                .language(siteHelper.getCurrentLocale().toString())
                .currency(siteHelper.getCurrentCurrency())
                .customer(getCustomer(page))
                .order(getEnrichedOrderForTracking(page).orElse(null))
                .campaign(buildCampaignForTracking())
                .csrf(sessionAuthenticationHelper.getCSRFToken())
                .productReplacementType(getReplacementType())
                .replacedProductCode(getReplacedProductCode())
                .loyalty(loyaltyTrackingService.getInfos().orElse(null))
                .cookieEmail(cookieService.getUserEmail().orElse(null))
                .currentUrl(sessionUrlHelper.getCurrentUrl())
                .cryptedEmails(encryptionEmailHelper.getEncryptedEmails())
                .build();
    }

    private CampaignForTracking buildCampaignForTracking() {
        CampaignForTracking campaignForTracking = new CampaignForTracking();
        Optional.ofNullable(sessionCampaignHelper.getCampaign()).ifPresent(campaignData -> {
            campaignForTracking.setCamId(campaignData.getCamId());
            campaignForTracking.setSourceId(campaignData.getSourceId());
            campaignForTracking.setVariationId(campaignData.getVariationId());
            campaignForTracking.setRetargetable(campaignData.isRetargetable());
            campaignForTracking.setEndDate(campaignData.getEndDate());
        });
        Optional.ofNullable(sessionCampaignHelper.getProfileData()).map(ProfileData::getProfileForTracking).ifPresent(profileForTracking -> {
            campaignForTracking.setCustomerStage(profileForTracking.getCustomerStage());
            campaignForTracking.setDistributionSpace(profileForTracking.getDistributionSpace());
            campaignForTracking.setSourceId(profileForTracking.getSource());
            campaignForTracking.setVadId(profileForTracking.getVadId());
            campaignForTracking.setRetailId(profileForTracking.getRetailId());
            campaignForTracking.setSegmentVad(profileForTracking.getSegmentVad());
            campaignForTracking.setClientType(profileForTracking.getClientType());
        });
        utmDatalayerHelper.saveSessionUtmParametresAndValues();
        Optional.ofNullable(utmDatalayerHelper.getUtmParametresAndValues()).ifPresent(campaignForTracking::setUtm);
        return campaignForTracking;
    }

    private Optional<CartTracking> getEnrichedCartForTracking() {

        CartWsDTO cartWsDTO = getCartForTracking(null);
        if (cartWsDTO == null) {
            return Optional.empty();
        }

        return buildCartWithRewardTracking(cartWsDTO);
    }

    private Optional<CartTracking> buildCartWithRewardTracking(CartWsDTO cartWsDTO) {
        try {
            CartTracking cartTracking = new CartTracking();
            BeanUtils.copyProperties(cartTracking, cartWsDTO);
            cartTracking.setRewardTracking(RewardTracking.builder()
                    .selectedAdvantages(selectedAdvantageTrackingService.getSelectedAdvantages()).build());

            return Optional.of(cartTracking);
        } catch (InvocationTargetException | IllegalAccessException exception) {
            log.warn(exception.getMessage(), exception);
            return Optional.empty();
        }
    }

    private Optional<OrderTracking> getEnrichedOrderForTracking(Page page) {

        OrderWsDTO orderTracking = getOrder(page);
        if (orderTracking == null) {
            return Optional.empty();
        }

        return buildOrderWithRewardTracking(orderTracking);
    }

    private Optional<OrderTracking> buildOrderWithRewardTracking(OrderWsDTO orderWsDTO) {
        try {
            OrderTracking orderTracking = new OrderTracking();
            BeanUtils.copyProperties(orderTracking, orderWsDTO);

            loyaltyConfirmPurchaseTrackingService.getInfos().ifPresent(orderTracking::setLoyaltyRewards);

            return Optional.of(orderTracking);
        } catch (InvocationTargetException | IllegalAccessException exception) {
            log.warn(exception.getMessage(), exception);
            return Optional.empty();
        }
    }

    public CartWsDTO getCartForTracking(CartWsDTO cartWsDto) {
        CartWsDTO cart = cartWsDto;
        if (isNull(cart)) {
            cart = sessionCartHelper.getCartForTracking();
        }
        if (Objects.nonNull(cart) && CollectionUtils.isNotEmpty(cart.getAppliedVouchers())) {
            CartWsDTO clonedCart = new CartWsDTO();
            VoucherWsDTO clonedVoucher = new VoucherWsDTO();
            try {
                clonedCart = (CartWsDTO) BeanUtils.cloneBean(cart);
                clonedVoucher = (VoucherWsDTO) BeanUtils.cloneBean(cart.getAppliedVouchers().get(0));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.warn("clone not supported : {}, cause {}", e.getMessage(), e);
            }

            if (clonedVoucher.isGrHidden()) {
                clonedVoucher.setCode(clonedVoucher.getGrTrackingCode());
                clonedVoucher.setVoucherCode(clonedVoucher.getGrTrackingCode());
                clonedVoucher.setGrTrackingCode(null);
                clonedCart.setAppliedVouchers(Collections.singletonList(clonedVoucher));

                return clonedCart;
            }
        }
        return cart;
    }

    public OrderWsDTO getOrderForTracking(OrderWsDTO orderWsDto) {

        if (Objects.nonNull(orderWsDto) && CollectionUtils.isNotEmpty(orderWsDto.getAppliedVouchers())) {
            OrderWsDTO clonedOrder = new OrderWsDTO();
            VoucherWsDTO clonedVoucher = new VoucherWsDTO();
            try {
                clonedOrder = (OrderWsDTO) BeanUtils.cloneBean(orderWsDto);
                clonedVoucher = (VoucherWsDTO) BeanUtils.cloneBean(orderWsDto.getAppliedVouchers().get(0));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.warn("clone not supported : {}, cause {}", e.getMessage(), e);
            }

            if (clonedVoucher.isGrHidden()) {
                clonedVoucher.setCode(clonedVoucher.getGrTrackingCode());
                clonedVoucher.setVoucherCode(clonedVoucher.getGrTrackingCode());
                clonedVoucher.setGrTrackingCode(null);
                clonedOrder.setAppliedVouchers(Collections.singletonList(clonedVoucher));

                return clonedOrder;
            }
        }
        return orderWsDto;
    }

    private CustomerForTracking getCustomer(Page page) {
        return sessionUserHelper.getCustomer().map(c -> customerForTrackingMapper.getCustomer(c, page)).orElse(null);
    }

    private String getReplacementType() {
        final String replacementType = request.getParameter("replacementType");
        return StringUtils.defaultIfBlank(replacementType, StringUtils.EMPTY);
    }

    private String getReplacedProductCode() {
        final String replacedProduct = request.getParameter("fromProduct");
        return StringUtils.defaultIfBlank(replacedProduct, StringUtils.EMPTY);
    }

    public String getReference(CmsComponent component) {
        if (component == null) {
            return StringUtils.EMPTY;
        }

        String reference = StringUtils.defaultString(component.getTypeCode(), component.getClass().getSimpleName());
        if (StringUtils.isNotBlank(component.getName())) {
            reference += "-" + component.getName();
        }

        return StringUtils.lowerCase(reference).replace(" ", "-");
    }

    public Map<String, CmsComponent> componentAsMap(Page page) {
        final Set<CmsComponent> cmsComponents = slotHelper.contentSlotsAsMap(page).values().stream()
                .map(ContentSlot::getComponents)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return cmsComponents.stream()
                .collect(Collectors.toMap(this::getReference, c -> c, (c1, c2) -> c1));
    }

    /**
     * @param page a page like homepage or summary page
     * @return orderWsDTO if page is summary page
     */
    private OrderWsDTO getOrder(Page page) {
        return sessionUserHelper.getCustomer()
                .map((CustomerDTO c) -> {
                    final Optional<CmsComponent> component = slotHelper.getComponent(page, SummaryComponent.class.getSimpleName());
                    if (component.isPresent()) {
                        final SummaryComponent summaryComponent = (SummaryComponent) component.get();
                        return summaryComponent.getOrder();
                    }
                    return null;
                }).orElse(null);
    }

}
