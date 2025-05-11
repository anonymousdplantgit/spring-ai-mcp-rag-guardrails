import { Component, OnInit, signal } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Table, TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { ToolbarModule } from 'primeng/toolbar';
import { RatingModule } from 'primeng/rating';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputNumberModule } from 'primeng/inputnumber';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { InputIconModule } from 'primeng/inputicon';
import { IconFieldModule } from 'primeng/iconfield';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { lastValueFrom } from 'rxjs';
import { LazyLoadingEventRequest } from '../../@core/api/generated/models/lazy-loading-event-request';

import { LoaderService } from '../../@core/services/loader.service';
import { DialogService } from 'primeng/dynamicdialog';
import { LocalizationService } from '../../@core/i18n/localization.service';
import { routePaths } from '../../../app.routes';
import { ConversationResource } from '../../@core/api/generated/models/conversation-resource';
import { ConversationsService } from '../../@core/api/generated/services/conversations.service';
import { Paginator } from 'primeng/paginator';
import { ConversationCardComponent } from './conversation-card.component';

@Component({
    selector: 'app-conversations',
    standalone: true,
    imports: [
        CommonModule,
        TableModule,
        FormsModule,
        ButtonModule,
        RippleModule,
        ToastModule,
        ToolbarModule,
        RatingModule,
        InputTextModule,
        TextareaModule,
        SelectModule,
        DialogModule,
        TagModule,
        InputIconModule,
        IconFieldModule,
        ConfirmDialogModule,
        InputNumberModule,
        RadioButtonModule,
        ReactiveFormsModule,
        Paginator,
        ConversationCardComponent
    ],
    template: `
        <p-toolbar styleClass="mb-6">
            <ng-template #start>
                <p-button label="New" icon="pi pi-plus" severity="secondary" class="mr-2" (onClick)="openForm()" />
            </ng-template>

            <ng-template #end>
                <div class="flex flex-end gap-2">
                    <p-button icon="pi pi-sync" severity="secondary" (onClick)="loadData(event)" />
                </div>
            </ng-template>
        </p-toolbar>
        <div class="grid grid-cols-12 gap-4">
            @for (item of items(); track item.id) {
                <div class="col-span-12">
                    <app-conversation-card (click)="edit(item)" [item]="item"></app-conversation-card>
                </div>
            }
        </div>
        <p-paginator
            styleClass="mt-2"
            currentPageReportTemplate="Showing {first} to {last} of {totalRecords} services"
            [showCurrentPageReport]="true"
            (onPageChange)="loadData($event)"
            [first]="event.first!"
            [rows]="event.rows!"
            [totalRecords]="totalRecords()"
            [rowsPerPageOptions]="[7, 11, 16, 20]"
        />


    `,
    providers: [DialogService,MessageService, ConfirmationService]
})
export class BotsComponent implements OnInit {
    items = signal<ConversationResource[]>([]);
    selectedItems!: ConversationResource[] | null;
    totalRecords = signal<number>(0);
    event: LazyLoadingEventRequest = {rows:5, first:0};
    selectedChatBot?: ConversationResource;
    systemMessage?: String
    protected readonly routePaths = routePaths;

    constructor(
        private localizationService: LocalizationService,
        private dialogService: DialogService,
        private conversationsService: ConversationsService,
        public loaderService: LoaderService
    ) {}

    private _showChat = signal<boolean>(false);

    get showChat() {
        return this._showChat();
    }

    set showChat(value: boolean) {
        this._showChat.set(value);
    }

    exportCSV() {

    }

    ngOnInit() {
        this.loadData(this.event)
    }

    edit(item: ConversationResource) {

    }

    delete(item: ConversationResource) {

    }

    async loadData($event: any) {
        this.event = $event;
        if (this.event) {
            const response = await lastValueFrom(this.conversationsService.getConversations({ request: this.event }));
            this.items.set(response.content!);
            this.totalRecords.set(response.totalElements!);
        }
    }

    onGlobalFilter(table: Table, event: Event) {
        table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
    }

    openForm(item?: ConversationResource) {

    }

    chatWith(item: ConversationResource) {
        this.showChat = true;
        this.selectedChatBot = item;
    }
}
