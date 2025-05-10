import { Component, Input } from '@angular/core';
import { Card } from 'primeng/card';

@Component({
    selector: 'app-no-items-found',
    template: ` <p-card class="surface-ground text-center"><i class="pi pi-exclamation-triangle"></i> {{ message }}</p-card> `,
    imports: [Card],
    standalone: true
})
export class NoItemsFoundComponentComponent {
    @Input() message: string = 'No item found';
}
