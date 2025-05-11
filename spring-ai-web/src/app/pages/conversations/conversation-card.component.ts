import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { routePaths } from '../../../app.routes';
import { Card } from 'primeng/card';
import { ConversationResource } from '../../@core/api/generated/models/conversation-resource';
import { AnimateOnScroll } from 'primeng/animateonscroll';
import { DatePipe } from '@angular/common';
import { Tag } from 'primeng/tag';
import { Rating } from 'primeng/rating';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-conversation-card',
    standalone: true,
    template: `
        <p-card
            styleClass="surface-ground cursor-pointer hover:shadow-xl transition-shadow duration-300"
            pAnimateOnScroll
            enterClass="animate-enter fade-in-10 slide-in-from-r-80 animate-duration-1000"
            leaveClass="animate-leave fade-out-0"
            (mouseenter)="hovered.set(true)"
            (mouseleave)="hovered.set(false)"
        >

            <div class="grid grid-cols-12 gap-4">
                <div class="col-span-12  sm:col-span-3">
                    <div class="flex flex-col gap-1">
                        <span class="text-xs text-muted-color-emphasis">{{item.id}}</span>
                        <span>{{item.startTime| date:'mediumDate'}} {{item.startTime| date:'shortTime'}}</span>
                    </div>
                </div>
                <div class="col-span-12  sm:col-span-3">
                    <div class="w-full h-full  align-middle flex justify-center items-center">
                        <p-tag value="{{item.status}}"></p-tag>
                    </div>

                </div>
                <div class="col-span-12  sm:col-span-3">
                    <div class="w-full h-full  align-middle flex justify-center items-center">
                   <p-rating [stars]="5" readonly  [ngModel]="3">
                       <ng-template #onicon>
                           <i class="pi pi-star-fill text-blue-400" style="font-size: 2rem"></i>
                       </ng-template>
                       <ng-template #officon>
                           <i class="pi pi-star text-blue-200" style="font-size: 2rem"></i>
                       </ng-template>
                   </p-rating>
                    </div>
                </div>
                <div class="col-span-12  sm:col-span-3">
                    <p-tag value="{{item.status}}"></p-tag>
                </div>
            </div>
        </p-card>
    `,
    imports: [
        Card,
        AnimateOnScroll,
        DatePipe,
        Tag,
        Rating,
        FormsModule
    ]
})
export class ConversationCardComponent {
    @Output() open = new EventEmitter();
    @Input() item!: ConversationResource;
    @Input() edit = false;
    @Input() view = true;
    hovered = signal(false);
    protected readonly routePaths = routePaths;

}
