import {Routes} from '@angular/router';
import {AppLayout} from './app/layout/component/app.layout';
import {DashboardComponent} from './app/pages/dashboard/dashboard.component';
import {Landing} from './app/pages/landing/landing';
import {Notfound} from './app/pages/notfound/notfound';
import {MenuItem} from 'primeng/api';
import {SessionService} from './app/@core/services/session.service';
import {BotsComponent} from "./app/pages/bots/bots.component";
import {BotDetailsComponent} from "./app/pages/bots/bot-details.component";

export const routePaths = {
    auth: {
        login: '/auth/login',
        register: '/auth/register',
        forgotPassword: '/auth/forgot-password',
        resetPassword: '/auth/reset-password'
    },
    public: {
        sendReview: '/public/send-review'
    },
    private: {
        home: '/',
        bots: {
            list: '/bots',
            details: '/bots/details/'
        },
        organisations: {
            list: '/organisations'
        }
    }
};
export const appRoutes: Routes = [
    {
        path: '',
        component: AppLayout,
        children: [
            { path: '', component: DashboardComponent },
            { path: 'bots',
                children: [
                    { path: '', component: BotsComponent },
                    { path: 'details/:chatbotId', component: BotDetailsComponent },
                ]
            },
        ]
    },
    { path: 'landing', component: Landing },
    { path: 'notfound', component: Notfound },
    { path: 'auth', loadChildren: () => import('./app/pages/auth/auth.routes') },
    { path: '**', redirectTo: '/notfound' }
];

export const menuRoutes = (sessionService: SessionService): MenuItem[] => {
    return [
        {
            items: [
                { label: 'Dashboard', icon: 'pi pi-fw pi-home', routerLink: ['/'] },
                { label: 'Bots', icon: 'pi pi-fw pi-android',
                    routerLink: [routePaths.private.bots.list] },
            ]
        }
    ];
};
