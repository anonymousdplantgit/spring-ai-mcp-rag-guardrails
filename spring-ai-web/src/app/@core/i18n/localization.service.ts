import {Injectable, Optional, signal, SkipSelf} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {LocalizationServiceConfig} from './localization-config.service';
import {lastValueFrom, Subject} from 'rxjs';
import {SessionService} from '../services/session.service';
import {registerLocaleData} from '@angular/common';
import localeEn from '@angular/common/locales/en';
import localeTr from '@angular/common/locales/tr';
import localeAr from '@angular/common/locales/ar';
import localeDe from '@angular/common/locales/de';
import {LanguageEnum} from "../api/generated/models";
// import localeFr from '@angular/common/locales/fr'
// import localeRu from '@angular/common/locales/ru'
// import localePs from '@angular/common/locales/ps'
// import localeFa from '@angular/common/locales/fa'
// import localeIt from '@angular/common/locales/it'
// import localePl from '@angular/common/locales/pl'
// import localeSo from '@angular/common/locales/so'
// import localeSq from '@angular/common/locales/sq'

export const supportedLanguages = [LanguageEnum.En, LanguageEnum.Fr, LanguageEnum.Nl, LanguageEnum.De];

/**
 * Class representing the translation service.
 */
@Injectable({
    providedIn: 'root'
})
export class LocalizationService {
    currentLocalId = signal<string>('en');
    private languageChanged = new Subject<string>();
    languageChanges$ = this.languageChanged.asObservable();

    /**
     * @constructor
     * @param {LocalizationService} singleton - the localization service
     * @param sessionService
     * @param {LocalizationServiceConfig} config - the localization config
     * @param {TranslateService} translateService - the translate service
     */
    constructor(
        @Optional() @SkipSelf() private singleton: LocalizationService,
        private sessionService: SessionService,
        private config: LocalizationServiceConfig,
        private translateService: TranslateService
    ) {
        if (this.singleton) {
            throw new Error('LocalizationService is already provided by the root module');
        }
        this.currentLocalId.set(this.config.locale_id);
    }

    get deviceLanguageOrDefault() {
        const deviceLanguage = navigator.language.includes('-') ? navigator.language.split('-')[0] : navigator.language;
        return supportedLanguages.includes(deviceLanguage as LanguageEnum) ? deviceLanguage : this.config.locale_id;
    }

    get isRtl() {
        return document.body.getAttribute('dir') === 'rtl';
    }

    /**
     * Initialize the service.
     * @returns {Promise<void>}
     */
    public initService(): Promise<void> {
        // language code same as file name.
        //  this._localeId = this.sessionService.getUserLang() || (this.isNavigatorLangSupported ? navigator.language : this.config.locale_id)
        console.log('default config for LOCAL_ID', this.config.locale_id);
        console.log('Device language', navigator.language);
        console.log('current user language', this.sessionService.getUserLang());
        this.currentLocalId.set(this.sessionService.getUserLang() || this.deviceLanguageOrDefault);
        console.log('Loading localData and using local', this.currentLocalId());
        this.loadLocaleData(this.currentLocalId());
        return this.useLanguage(this.currentLocalId());
    }

    setDefaultLocale(lang: string) {
        this.translateService.setDefaultLang(lang);
    }

    toggleDirection(lang: string) {
        const currentDirection = document.body.getAttribute('dir');
        if (lang === 'ar' && currentDirection !== 'rtl') {
            document.body.setAttribute('dir', 'rtl');
        } else if (currentDirection === 'rtl') {
            document.body.setAttribute('dir', 'ltr');
        }
    }

    /**
     * change the selected language
     * @returns {Promise<void>}
     */
    public async useLanguage(lang: string): Promise<void> {
        this.currentLocalId.set(lang);
        console.log('change language', this.currentLocalId());
        this.setDefaultLocale(lang);
        this.sessionService.storeUserLang(lang);
        this.toggleDirection(lang);
        try {
            // @ts-ignore
            return await lastValueFrom(this.translateService.use(lang));
        } catch {
            this.sessionService.removeUserLang();
            throw new Error('LocalizationService.init failed');
        }
    }

    public usedLanguage(): string {
        return this.translateService.currentLang;
    }

    public translate(key: string | string[], interpolateParams?: object) {
        return this.translateService.instant(key, interpolateParams);
    }

    public changeLanguage(language: string) {
        this.sessionService.storeUserLang(language);
        this.useLanguage(language).then(() => {
            this.languageChanged.next(language);
        });
    }

    public changeLanguageWithoutReloading(language: string) {
        this.useLanguage(language).then(() => {
            this.languageChanged.next(language);
        });
    }

    private loadLocaleData(locale: string) {
        switch (locale) {
            case 'ar':
                registerLocaleData(localeAr);
                break;
            case 'en':
                registerLocaleData(localeEn);
                break;
            case 'de':
                registerLocaleData(localeDe);
                break;
            case 'tr':
                registerLocaleData(localeTr);
                break;
            // Add cases for other locales as needed
            default:
                registerLocaleData(localeEn);
        }
    }
}
