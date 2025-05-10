import { APP_INITIALIZER, ModuleWithProviders, NgModule } from '@angular/core'
import { CommonModule } from '@angular/common'
import { LocalizationService } from './localization.service'
import { LocalizationServiceConfig } from './localization-config.service'
import { TranslateModule } from '@ngx-translate/core'
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http'

@NgModule({ declarations: [],
    exports: [TranslateModule], imports: [CommonModule, TranslateModule.forChild()], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class InternationalizationModule {
    public static forRoot(config: any): ModuleWithProviders<InternationalizationModule> {
        return {
            ngModule: InternationalizationModule,
            providers: [
                {
                    provide: APP_INITIALIZER,
                    useFactory: initLocalizationService,
                    deps: [LocalizationService],
                    multi: true,
                },
                LocalizationService,

                // using the initial value
                { provide: LocalizationServiceConfig, useValue: config },
            ],
        }
    }
}

/**
 * Initialize the localization service.
 * @param {LocalizationService} service
 * @returns {() => Promise<void>}
 */
export function initLocalizationService(service: LocalizationService) {
    return () => service.initService()
}
