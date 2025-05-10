import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const isInvalid = (control: AbstractControl) => {
    return control?.invalid && (control?.dirty || control?.touched);
};
export const passwordMatchingValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');
    return password?.value && confirmPassword?.value && password.value !== confirmPassword.value ? { notmatched: true } : null;
};

export function isValidEmail(email?: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return email ? emailRegex.test(email) : false;
}

export function getStarCategory(stars: number) {
    if (stars >= 0 && stars < 2) {
        return 'danger';
    } else if (stars >= 2 && stars <= 3) {
        return 'warn';
    } else if (stars > 3) {
        return 'success';
    } else {
        throw Error('Incorrect Overall experience value');
    }
}

export function getPercentageColor(stars: number) {
    if (stars <= 1) {
        return { text: 'text-red-500', bg: 'bg-red-500' };
    } else if (stars > 1 && stars <= 2) {
        return { text: 'text-orange-500', bg: 'bg-orange-500' };
    } else if (stars >= 2 && stars <= 3) {
        return { text: 'text-yellow-500', bg: 'bg-yellow-500' };
    } else if (stars >= 3 && stars <= 4) {
        return { text: 'text-blue-500', bg: 'bg-blue-500' };
    } else if (stars > 3) {
        return { text: 'text-green-500', bg: 'bg-green-500' };
    } else {
        throw Error('Incorrect Overall experience value');
    }
}
