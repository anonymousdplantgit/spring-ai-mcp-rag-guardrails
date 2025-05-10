import { HttpClient, provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, enableProdMode, importProvidersFrom, LOCALE_ID } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling } from '@angular/router';
import Aura from '@primeng/themes/aura';
import { providePrimeNG } from 'primeng/config';
import { appRoutes } from './app.routes';
import { HashLocationStrategy, LocationStrategy, registerLocaleData } from '@angular/common';
import { authInterceptor } from './app/@core/interceptors/auth-interceptor';
import { loaderInterceptor } from './app/@core/interceptors/loader-interceptor';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ApiModule } from './app/@core/api/generated/api.module';
import { environment } from './environments/environment';
import { LocalizationService } from './app/@core/i18n/localization.service';
import { InternationalizationModule } from './app/@core/i18n/internationalization.module';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MultiTranslateHttpLoader } from './app/@core/i18n/multi-translate-http-loader';
import localeFr from '@angular/common/locales/fr';
import localeEn from '@angular/common/locales/en';
import localeDe from '@angular/common/locales/de';
import localeNl from '@angular/common/locales/nl';

if (environment.production) {
    enableProdMode();
}
export const appConfig: ApplicationConfig = {
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        {
            provide: LOCALE_ID,
            deps: [LocalizationService],
            useFactory: (localizationService: LocalizationService) => {
                console.log('Initializing LOCALE_ID with value ', localizationService.currentLocalId());
                return localizationService.currentLocalId();
            }
        },
        provideRouter(
            appRoutes,
            withInMemoryScrolling({
                anchorScrolling: 'enabled',
                scrollPositionRestoration: 'enabled'
            }),
            withEnabledBlockingInitialNavigation()
        ),
        provideHttpClient(withFetch(), withInterceptors([loaderInterceptor, authInterceptor])),
        provideAnimationsAsync(),
        providePrimeNG({ theme: { preset: Aura, options: { darkModeSelector: '.app-dark' } } }),
        importProvidersFrom(
            BrowserAnimationsModule,
            InternationalizationModule.forRoot({ locale_id: 'en' }),
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useFactory: HttpLoaderFactory,
                    deps: [HttpClient]
                }
            }),
            ApiModule.forRoot({ rootUrl: environment.apiBasePath })
        )
    ]
};

export function HttpLoaderFactory(httpClient: HttpClient) {
    const folders = ['app'];
    return new MultiTranslateHttpLoader(httpClient, {
        withCommon: true,
        resources: folders.map((folder) => {
            return {
                prefix: `./assets/i18n/${folder}/`,
                suffix: '.json'
            };
        })
    });
}

registerLocaleData(localeEn);
registerLocaleData(localeFr);
registerLocaleData(localeDe);
registerLocaleData(localeNl);
