// model for a resource to load
import { TranslateLoader } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export type Resource = { prefix: string; suffix: string };

export class MultiTranslateHttpLoader implements TranslateLoader {
    resources: Resource[];
    withCommon: boolean;

    constructor(
        private readonly http: HttpClient,
        { resources, withCommon = true }: { resources: Resource[]; withCommon?: boolean }
    ) {
        this.resources = resources;
        this.withCommon = withCommon;
    }

    getTranslation(lang: string): Observable<Record<string, unknown>> {
        let resources: Resource[] = [...this.resources];

        if (this.withCommon) {
            // order matters! like this, all translations from common can be overrode with features' translations
            resources = [{ prefix: './assets/i18n/common/', suffix: '.json' }, ...resources];
        }

        return forkJoin(
            resources.map((config: Resource) => {
                return this.http.get<Record<string, unknown>>(`${config.prefix}${lang}${config.suffix}`);
            })
        ).pipe(map((response: Record<string, unknown>[]) => mergeObjectsRecursively(response)));
    }
}

export const mergeObjectsRecursively = (objects: Record<string, unknown>[]): Record<string, unknown> => {
    const mergedObject: Record<string, unknown> = {};

    for (const obj of objects) {
        for (const key in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, key)) {
                if (typeof obj[key] === 'object' && obj[key] !== null) {
                    mergedObject[key] = mergeObjectsRecursively([
                        // @ts-ignore
                        mergedObject[key],
                        // @ts-ignore
                        obj[key]
                    ]);
                } else {
                    mergedObject[key] = obj[key];
                }
            }
        }
    }

    return mergedObject;
};
