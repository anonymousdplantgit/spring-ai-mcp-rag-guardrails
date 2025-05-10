import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, computed, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PrimeNG } from 'primeng/config';
import { SelectButtonModule } from 'primeng/selectbutton';
import { LocalizationService, supportedLanguages } from '../../@core/i18n/localization.service';
import { Button } from 'primeng/button';

@Component({
    selector: 'app-language-selector',
    standalone: true,
    imports: [CommonModule, FormsModule, SelectButtonModule, Button],
    template: `
        <div class="flex flex-col gap-4">
            <div>
                <div class="pt-2 flex gap-2 flex-col">
                    @for (language of supportedLanguages; track language) {
                        <p-button
                            styleClass="uppercase"
                            fluid
                            [severity]="language === selectedLanguage() ? 'info' : 'secondary'"
                            [icon]="language === selectedLanguage() ? 'pi pi-check-circle' : 'pi pi-language'"
                            type="button"
                            label="{{ language }}"
                            [title]="language"
                            (click)="localizationService.changeLanguage(language)"
                        ></p-button>
                    }
                </div>
            </div>
        </div>
    `,
    host: {
        class: 'hidden absolute top-[3.25rem] right-0 w-32 p-4 bg-surface-0 dark:bg-surface-900 border border-surface rounded-border origin-top shadow-[0px_3px_5px_rgba(0,0,0,0.02),0px_0px_2px_rgba(0,0,0,0.05),0px_1px_4px_rgba(0,0,0,0.08)]'
    }
})
export class AppLanguageSelector implements OnInit {
    router = inject(Router);

    config: PrimeNG = inject(PrimeNG);

    localizationService: LocalizationService = inject(LocalizationService);

    platformId = inject(PLATFORM_ID);

    primeng = inject(PrimeNG);

    selectedLanguage = computed(() => {
        return this.localizationService.currentLocalId();
    });
    protected readonly supportedLanguages = supportedLanguages;

    ngOnInit() {
        if (isPlatformBrowser(this.platformId)) {
        }
    }

    updateColors(event: any, type: string, color: any) {
        event.stopPropagation();
    }
}
