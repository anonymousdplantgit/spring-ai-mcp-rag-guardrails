import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { lastValueFrom } from 'rxjs';
import { UserResource } from '../../@core/api/generated/models/user-resource';
import { NgIf } from '@angular/common';
import { routePaths } from '../../../app.routes';
import { PublicUsersService } from '../../@core/api/generated/services/public-users.service';

@Component({
    selector: 'app-validate',
    imports: [ButtonModule, RippleModule, RouterModule, AppFloatingConfigurator, ButtonModule, NgIf],
    standalone: true,
    template: ` <app-floating-configurator />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div *ngIf="validatedUser" style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20 flex flex-col items-center" style="border-radius: 53px">
                        <div class="gap-4 flex flex-col items-center">
                            <div class="flex justify-center items-center border-2 border-green-500 rounded-full" style="height: 3.2rem; width: 3.2rem">
                                <i class="pi pi-fw pi-check-circle !text-2xl text-green-500"></i>
                            </div>
                            <h1 class="text-surface-900 dark:text-surface-0 font-bold text-5xl mb-2">You account has been validated successfully</h1>
                            <span class="text-muted-color mb-8">You can now login and start using the platform features</span>
                            <div class="col-span-12 mt-8 text-center">
                                <p-button label="Go to login" [routerLink]="routePaths.auth.login" />
                            </div>
                        </div>
                    </div>
                </div>
                <div *ngIf="!validatedUser" style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, rgba(233, 30, 99, 0.4) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20 flex flex-col items-center" style="border-radius: 53px">
                        <div class="gap-4 flex flex-col items-center">
                            <div class="flex justify-center items-center border-2 border-pink-500 rounded-full" style="height: 3.2rem; width: 3.2rem">
                                <i class="pi pi-fw pi-exclamation-circle !text-2xl text-pink-500"></i>
                            </div>
                            <h1 class="text-surface-900 dark:text-surface-0 font-bold text-5xl mb-2">No token found</h1>
                            <span class="text-muted-color mb-8">Requested resource is not available.</span>
                            <div class="col-span-12 mt-8 text-center">
                                <p-button label="Go to Login" [routerLink]="routePaths.auth.login" severity="danger" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>`
})
export class ValidateComponent implements OnInit {
    token: string | null;
    validatedUser?: UserResource;
    protected readonly routePaths = routePaths;

    constructor(
        private route: ActivatedRoute,
        private publicUsersService: PublicUsersService
    ) {
        this.token = this.route.snapshot.queryParamMap.get('token'); // Replace 'yourParam' with the actual query param name
    }

    ngOnInit(): void {
        this.validate();
    }

    async validate() {
        if (this.token) {
            this.validatedUser = await lastValueFrom(
                this.publicUsersService.validateUser({
                    token: this.token
                })
            );
        }
    }
}
