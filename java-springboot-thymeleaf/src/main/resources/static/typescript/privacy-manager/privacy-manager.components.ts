import * as $ from 'jquery';
// @ts-ignore:  TS can't find package 'js-cookies'
import * as Cookies from 'js-cookie';

export class PrivacyManager {
    private static privacyManager: HTMLElement | null;
    private static form: HTMLFormElement | null;
    private static cookiesLevelChoices: Array<HTMLElement> | null;
    private static cookieParameterToggle: HTMLElement | null;
    private static dataTrackingLevel: string | null;
    private static cookiesRetentionPeriodInDays: number;
    constructor() {
        PrivacyManager.privacyManager = document.querySelector( '[data-js="privacy-manager"]' );
        PrivacyManager.form = document.querySelector( '[data-js="privacy-manager_form"]' );
        PrivacyManager.cookiesLevelChoices = Array.from( document.querySelectorAll( '[data-js-cookie-level-choice]' ) );
        PrivacyManager.cookieParameterToggle = document.querySelector( '[data-js-label-default]' );
        PrivacyManager.cookiesRetentionPeriodInDays = 365;
    }
    private static handleCookieLevelSubmission() {
        if ( PrivacyManager.form ) {

            PrivacyManager.form.addEventListener(
                'submit',
                ( event: Event ) => {

                    // Block submission to prepare ajax transaction
                    event.preventDefault();

                    // Get user choice
                    const checkedInput: HTMLInputElement | null = document.querySelector( '[data-js="privacy-manager_input"]:checked' );

                    checkedInput ?
                        PrivacyManager.dataTrackingLevel = checkedInput.value :
                        null;

                    // Generate a unique ID for opt-in
                    const uuidv4 = require( 'uuid/v4' );
                    const cookieId = uuidv4();

                    // Get time
                    let cookieDate = Date.now();

                    // Build data
                    const cookieName = 'privacyManager';
                    const valuesAsArrayOfStrings = [
                        PrivacyManager.dataTrackingLevel,
                        cookieId,
                        cookieDate,
                    ];
                    const cookieExpirationAfterPeriodInDays = { expires: PrivacyManager.cookiesRetentionPeriodInDays };

                    // Set data to cookie
                    Cookies.set(
                        cookieName,
                        valuesAsArrayOfStrings.toString(),
                        cookieExpirationAfterPeriodInDays,
                    );

                    // Push to datalayer

                    // @ts-ignore
                    const datalayer: Array<Object> = window.dataLayer;

                    if ( typeof datalayer != 'undefined' && datalayer.length > 0 ) {
                        datalayer.push( {
                            'event': 'gdprConsent',
                            'gtmUserConsentValue': PrivacyManager.dataTrackingLevel,
                            'gtmUserConsentId': cookieId,
                            'gtmUserConsentTimestamp': cookieDate,
                        } );
                    }

                    // Prepare data for controller
                    const privacyData: Object = {
                        "clientId": cookieId,
                        "level": PrivacyManager.dataTrackingLevel,
                        "timeStamp": cookieExpirationAfterPeriodInDays,
                    };

                    // Send data to controller
                    const formAction: string | undefined = PrivacyManager.form && PrivacyManager.form.action ? PrivacyManager.form.action : undefined;
                    const formMethod: string | undefined = PrivacyManager.form && PrivacyManager.form.method ? PrivacyManager.form.method : undefined;

                    $.ajax( {
                        url: formAction,
                        type: formMethod,
                        data: privacyData,
                    } )
                        .always( function () {

                            // Mask privacy manager whatever the ajax result was
                            PrivacyManager.privacyManager ?
                                PrivacyManager.privacyManager.classList.replace( 'inline-block', 'none' ) :
                                null;
                        } );
                },
            );

            // DOM elements in place, we display the popin
            PrivacyManager.privacyManager !== null
                ? PrivacyManager.privacyManager.classList.replace( 'none', 'inline-block' )
                : null;
        }
    }
    private static onClickConfigureCookies() {}

    init() {

        $( document ).ready( function () {

            PrivacyManager.onClickConfigureCookies();
            PrivacyManager.handleCookieLevelSubmission();
        } );
    }
}
