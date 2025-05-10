import { HttpErrorResponse } from '@angular/common/http';

export const handleResponseErrorMessage = (error: any) => {
    if (error instanceof HttpErrorResponse) {
        if (error.error && error.error.exceptionName) {
            return 'common.messages.' + error.error.exceptionName;
        } else if (error.error.messsage) {
            return error.error.messsage;
        } else {
            return error.statusText;
        }
    } else if (error.messsage) {
        return error.messsage;
    } else {
        return 'common.messages.server-error';
    }
};

export const handleValidationErrors = (errors: Record<string, string>): string[] => {
    return Object.entries(errors).map(([field, message]) => {
        const title = field.charAt(0).toUpperCase() + field.slice(1); // Capitalize the field name
        return `${title}: ${message}`;
    });
};

export const handleServerErrors = (error: Record<string, string>): string => {
    return error['message'];
};
