import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { inject, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';

export function loaderInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    const TIMEOUT_DURATION = 20000; // 20 seconds
    const loaderService = inject(LoaderService);
    const ngZone = inject(NgZone);
    // Skip loader for specific requests if needed
    if (req.headers.has('Skip-Loader')) {
        return next(req);
    }
    const requestUrl = req.url;
    loaderService.addRequest(requestUrl);
    // Set up timeout to automatically remove hanging requests
    const timeoutId = setTimeout(() => {
        loaderService.removeRequest(requestUrl);
        console.warn(`Request timeout for URL: ${requestUrl}`);
    }, TIMEOUT_DURATION);

    return next(req).pipe(
        finalize(() => {
            clearTimeout(timeoutId);
            ngZone.run(() => {
                loaderService.removeRequest(requestUrl);
            });
        })
    );
}
