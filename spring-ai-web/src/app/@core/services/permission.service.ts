import {inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateChildFn, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {SessionService} from './session.service';
import {routePaths} from '../../../app.routes';

export const canActivate: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
    return inject(PermissionsService).canActivate(next, state);
};

export const canActivateChild: CanActivateChildFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => canActivate(route, state);

@Injectable({
    providedIn: 'root'
})
class PermissionsService {
    redirectUrl = routePaths.auth.login;

    constructor(
        private router: Router,
        private sessionService: SessionService
    ) {
        console.log('PermissionsService initialized');
    }

    canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        const url: string = state.url;
        return this.checkLogin(url, next);
    }

    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        return this.canActivate(route, state);
    }

    checkLogin(url: string, route: ActivatedRouteSnapshot): boolean {
        const user = undefined;
        if (!user) {
            console.warn('User is not logged or required OTP, redirecting to auth screen');
            // this.router.navigate([this.redirectUrl], {queryParams: {returnUrl: url}});
            this.router.navigate([this.redirectUrl], {
                queryParams: { returnUrl: url }
            });
            return false;
        }
        const expectedRoles: any[] = route.data['expectedRoles'];
        console.warn('expectedRoles', JSON.stringify(expectedRoles));
        if (!expectedRoles) {
            return true;
        }

        console.log("User doesn't have required access rights- This routing guard prevents redirection to any routes that needs specific access rights.");
        // Store the original url in login service and then redirect to login page

        this.router.navigate([this.redirectUrl], {
            queryParams: { returnUrl: url }
        });
        console.log('next landingPage is : ', this.redirectUrl);
        return false;
    }

    private matchRole(expectedRoles: string[], roles: string[] | undefined): boolean {
        if (!expectedRoles || !roles) {
            return false;
        }
        const intersection = [];
        roles?.filter((element) => {
            if (expectedRoles.indexOf(element as string) != -1) {
                intersection.push(element);
            }
        });
        return expectedRoles.length == 0 || intersection.length > 0;
    }
}
