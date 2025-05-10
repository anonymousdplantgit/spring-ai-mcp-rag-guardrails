import {HttpEvent, HttpHandlerFn, HttpHeaders, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {inject} from '@angular/core';
import {SessionService} from '../services/session.service';
import {catchError} from 'rxjs/operators';
import {Router} from '@angular/router';
import {routePaths} from '../../../app.routes';
import {SharedToasterService} from '../services/shared.toaster.service';
import {handleServerErrors, handleValidationErrors} from '../../utils/error-utils';

export function authInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    const sessionService = inject(SessionService);
    const router = inject(Router);
    const sharedToasterService = inject(SharedToasterService);
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    let cloneReq = req.clone({
        headers: new HttpHeaders({
            'X-TimeZone': timeZone
        }),

        withCredentials: false
    });
    return next(cloneReq).pipe(
        //delay(0),
        catchError((error) => {
            console.log(error.error);
            if (error.status === 401 || error.status === 0) {
                sessionService.destroySession();
                router.navigate([routePaths.auth.login]);
            } else if (error.status === 400) {
                const formattedErrors = handleValidationErrors(error.error);
                formattedErrors.forEach((error) => {
                    sharedToasterService.actionError.next(error);
                });
            } else if (error.status === 500) {
                sharedToasterService.actionError.next(handleServerErrors(error.error));
            }
            return throwError(error);
        })
    );
}
