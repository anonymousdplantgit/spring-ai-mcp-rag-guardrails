import {Injectable} from '@angular/core';


@Injectable({
    providedIn: 'root'
})
export class SessionService {
    public storage: Storage = localStorage; // <--- you may switch between sessionStorage or LocalStorage (only one place to change)
    private appKey = 'jdma-';
    private currentLang: string;
    private jwtDataKey: string;
    private connectedUserKey: string;
    private connectedAsKey: string;
    private otpValidatedKey: string;
    private otpForgotPasswordEmailKey: string;
    private otpForgotPasswordValidatedKey: string;
    private expirationTimeKey: string;
    private currentLatLngKey: string;
    private currentCompanyKey: string;
    private paymentStripeSessionIdKey: string;
    private impersonatorKey: string;
    private colorSchemeKey: string;

    constructor() {
        this.currentLang = this.appKey + 'currentLang';
        this.jwtDataKey = this.appKey + 'jwtData';
        this.connectedUserKey = this.appKey + 'connectedUser';
        this.connectedAsKey = this.appKey + 'connectedAs';
        this.otpValidatedKey = this.appKey + 'otpValidated';
        this.otpForgotPasswordEmailKey = this.appKey + 'otpForgotPasswordEmail';
        this.otpForgotPasswordValidatedKey = this.appKey + 'otpForgotPasswordValidated';
        this.expirationTimeKey = this.appKey + 'expirationTime';
        this.currentLatLngKey = this.appKey + 'currentLatLng';
        this.currentCompanyKey = this.appKey + 'currentCompany';
        this.paymentStripeSessionIdKey = this.appKey + 'paymentStripeSessionId';
        this.impersonatorKey = this.appKey + 'impersonator';
        this.colorSchemeKey = this.appKey + 'colorScheme';

        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
        const prefersHighContrast = window.matchMedia('(prefers-contrast: more)');
        console.log('prefersDark', prefersDark.matches);
        console.log('prefersHighContrast', prefersHighContrast.matches);
        if (!this.colorScheme) {
            if (prefersDark.matches) {
                this.storeColorScheme('dark');
            } else {
                this.storeColorScheme('light');
            }
        }
    }

    get colorScheme() {
        return this.getData(this.colorSchemeKey);
    }



    get paymentStripeSessionI() {
        return this.getData(this.paymentStripeSessionIdKey);
    }

    get otpValidated(): boolean {
        return this.getData(this.otpValidatedKey);
    }

    get otpForgotPasswordValidated(): boolean {
        return this.getData(this.otpForgotPasswordValidatedKey);
    }

    get otpForgotPasswordEmail(): string {
        return this.getData(this.otpForgotPasswordEmailKey);
    }

    get currentLatLng(): { lat: number; lng: number } | undefined {
        return this.getData(this.currentLatLngKey);
    }



    get userCurrentLanguage() {
        return this.getData(this.currentLang);
    }



    storeColorScheme(theme: string) {
        this.storeData(this.colorSchemeKey, theme);
    }



    removeImpersonator() {
        this.removeItem(this.impersonatorKey);
    }

    storeData(key: string, data: any) {
        this.storage.setItem(this.encrypt(key), this.encrypt(JSON.stringify(data)));
    }

    removeItem(key: string) {
        this.storage.removeItem(this.encrypt(key));
    }

    getData(key: string) {
        const data = this.storage.getItem(this.encrypt(key));
        if (data !== undefined && data !== 'undefined' && data !== null) {
            const decryptedDataAsString = this.decrypt(data);
            return JSON.parse(decryptedDataAsString);
        }
    }

    storePaymentStripeSessionId(paymentStripeSessionId: string) {
        this.storeData(this.paymentStripeSessionIdKey, paymentStripeSessionId);
    }

    removePaymentStripeSessionId() {
        this.removeItem(this.paymentStripeSessionIdKey);
    }



    storeOtpValidated(validated: boolean) {
        this.storeData(this.otpValidatedKey, validated);
    }

    storeOtpForgotPasswordValidated(validated: boolean) {
        this.storeData(this.otpForgotPasswordValidatedKey, validated);
    }

    storeOtpForgotPasswordEmail(email: string) {
        this.storeData(this.otpForgotPasswordEmailKey, email);
    }


    // Remove userinfo from session storage
    destroySession() {
        const lang = this.getUserLang();
        const colorScheme = this.colorScheme;
        this.storage.clear();
        this.storeUserLang(lang);
        this.storeColorScheme(colorScheme);
    }



    removeData(key: string) {
        this.storage.removeItem(key);
    }

    // Store userinfo from session storage
    storeUserLang(lang: string) {
        this.storeData(this.currentLang, lang);
    }

    // Remove userinfo from session storage
    removeUserLang() {
        this.storage.removeItem(this.currentLang);
    }




    getUserLang() {
        return this.getData(this.currentLang);
    }

 

    private encrypt(txt: string): string {
        return txt;
        // return btoa(txt);
    }

    private decrypt(txtToDecrypt: string) {
        return txtToDecrypt;
        // return atob(txtToDecrypt);
    }
}
