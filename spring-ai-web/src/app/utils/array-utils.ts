import { Signal } from '@angular/core';

export const arrayAddOrReplace = (key: string, array: any[], newObj: any) => {
    const index = array.findIndex((obj) => obj[key] === newObj[key]);

    if (index > -1) {
        // If the item exists, replace it at the same index
        array[index] = { ...newObj };
    } else {
        // If the item does not exist, add it to the end
        array.push({ ...newObj });
    }

    return [...array]; // Return a new array reference to trigger Angular change detection if necessary
};

export const arrayRemove = (idKey: string, array: any[], idToRemove: string) => {
    return [...array.filter((obj) => obj[idKey] !== idToRemove)];
};

export const pushWithDelay = (array: Signal<any>, response: any[]) => {};
