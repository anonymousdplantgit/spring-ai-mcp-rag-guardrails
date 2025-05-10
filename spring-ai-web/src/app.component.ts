import { Component, OnDestroy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { environment } from './environments/environment';
import { Subscription } from 'rxjs';
import { SharedToasterService } from './app/@core/services/shared.toaster.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterModule, Toast],
    template: ` <p-toast [autoZIndex]="true" position="top-center"></p-toast>
        <router-outlet></router-outlet>`,
    providers: [MessageService]
})
export class AppComponent implements OnDestroy {
    actionSuccessSubscription!: Subscription;
    actionErrorSubscription!: Subscription;
    actionWarningSubscription!: Subscription;
    constructor(
        private sharedToasterService: SharedToasterService,
        private messageService: MessageService
    ) {
        console.log('environment.production', environment.production);
        console.log('environment.apiBasePath', environment.apiBasePath);
        this.subscribeToToast();
    }
    subscribeToToast() {
        this.actionSuccessSubscription = this.sharedToasterService.actionSuccess.subscribe((data: string) => {
            this.messageService.add({
                severity: 'success',
                detail: data,
                life: 5000
            });
        });
        this.actionErrorSubscription = this.sharedToasterService.actionError.subscribe((data: string) => {
            this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: data,
                life: 5000
            });
        });
        this.actionWarningSubscription = this.sharedToasterService.actionWarning.subscribe((data: string) => {
            this.messageService.add({
                severity: 'warn',
                summary: 'Warning',
                detail: data,
                life: 5000
            });
        });
    }
    ngOnDestroy(): void {
        if (this.actionSuccessSubscription) {
            this.actionSuccessSubscription.unsubscribe();
        }
        if (this.actionWarningSubscription) {
            this.actionWarningSubscription.unsubscribe();
        }
        if (this.actionErrorSubscription) {
            this.actionErrorSubscription.unsubscribe();
        }
    }
}
