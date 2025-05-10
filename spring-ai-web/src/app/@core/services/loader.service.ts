import { Injectable } from '@angular/core'
import { BehaviorSubject, distinctUntilChanged, Observable } from 'rxjs'

@Injectable({
    providedIn: 'root',
})
export class LoaderService {
    private readonly pendingRequests = new Set<string>()
    private readonly loading$ = new BehaviorSubject<boolean>(false)
    private readonly excludedUrlPatterns = new Set<string>()

    constructor() {
        this.excludeUrlPattern('./assets/i18n')
    }

    get isLoading$(): Observable<boolean> {
        return this.loading$.asObservable().pipe(
            distinctUntilChanged() // Only emit when value actually changes
        )
    }

    get isLoading(): boolean {
        return this.loading$.value
    }

    /**
     * Adds a URL pattern to exclude from loader
     * Examples:
     * - '/api/health' will match any URL containing '/api/health'
     * - '/api/v1/polling' will match any URL containing '/api/v1/polling'
     */
    excludeUrlPattern(urlPattern: string): void {
        this.excludedUrlPatterns.add(urlPattern.toLowerCase())
    }

    /**
     * Removes a previously added URL pattern from exclusion list
     */
    removeExcludedUrlPattern(urlPattern: string): void {
        this.excludedUrlPatterns.delete(urlPattern.toLowerCase())
    }

    /**
     * Clears all excluded URL patterns
     */
    clearExcludedUrlPatterns(): void {
        this.excludedUrlPatterns.clear()
    }

    addRequest(url: string): void {
        if (!this.shouldExcludeUrl(url)) {
            this.pendingRequests.add(url)
            this.updateLoadingState()
        }
    }

    removeRequest(url: string): void {
        const deleted = this.pendingRequests.delete(url)
        this.updateLoadingState()
    }

    reset(): void {
        this.pendingRequests.clear()
        this.loading$.next(false)
    }

    /**
     * Checks if the given URL should be excluded based on configured patterns
     */
    private shouldExcludeUrl(url: string): boolean {
        const lowercaseUrl = url.toLowerCase()
        return Array.from(this.excludedUrlPatterns).some((pattern) => lowercaseUrl.includes(pattern))
    }

    private updateLoadingState(): void {
        const newLoadingState = this.pendingRequests.size > 0
        if (this.loading$.value !== newLoadingState) {
            this.loading$.next(newLoadingState)
        }
    }
}
