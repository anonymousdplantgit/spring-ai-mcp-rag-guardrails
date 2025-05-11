import { Component, OnInit, signal, ViewChild } from '@angular/core';
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
import { NoItemsFoundComponentComponent } from '../../shared/components/no-items-found.component';
import { ChatbotResource } from '../../@core/api/generated/models/chatbot-resource';
import { ChatBotsService } from '../../@core/api/generated/services/chat-bots.service';
import { DialogService } from 'primeng/dynamicdialog';
import { LocalizationService } from '../../@core/i18n/localization.service';
import { BotFormDialogComponent } from './bot-form-dialog.component';
import { arrayAddOrReplace } from '../../utils/array-utils';
import { RouterLink } from '@angular/router';
import { routePaths } from '../../../app.routes';
import { ChatComponent } from '../chat/chat.component';
import { Drawer } from 'primeng/drawer';
import { Popover } from 'primeng/popover';

@Component({
    selector: 'app-bots',
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
        NoItemsFoundComponentComponent,
        RouterLink,
        ChatComponent,
        Drawer,
        Popover
    ],
    template: `
        <p-drawer header="Ask your question to {{selectedChatBot?.name}}" [(visible)]="showChat" position="right"
                  styleClass="!w-full  lg:!w-[40rem]">
            <app-chat *ngIf="showChat" [selectedChatBot]="selectedChatBot"></app-chat>
        </p-drawer>

        <p-toolbar styleClass="mb-6">
            <ng-template #start>
                <p-button label="New" icon="pi pi-plus" severity="secondary" class="mr-2" (onClick)="openForm()" />
            </ng-template>

            <ng-template #end>
                <div class="flex flex-end gap-2">
                    <p-button icon="pi pi-sync" severity="secondary" (onClick)="loadData(event)" />
                    <p-button label="Export" icon="pi pi-upload" severity="secondary" (onClick)="exportCSV()" />
                </div>
            </ng-template>
        </p-toolbar>

        <p-table
            #dt
            [lazy]="true"
            [totalRecords]="totalRecords()"
            [value]="items()"
            [rows]="10"
            [loading]="loaderService.isLoading"
            (onLazyLoad)="loadData($event)"
            [paginator]="true"
            [tableStyle]="{ 'min-width': '75rem' }"
            [(selection)]="selectedItems"
            [rowHover]="true"
            dataKey="id"
            currentPageReportTemplate="Showing {first} to {last} of {totalRecords} requests"
            [showCurrentPageReport]="true"
            [rowsPerPageOptions]="[10, 20, 30]"
        >
            <ng-template #caption>
                <div class="flex items-center justify-between">
                    <h5 class="m-0">Manage chatbots</h5>
                    <p-iconfield>
                        <p-inputicon styleClass="pi pi-search" />
                        <input pInputText type="text" (input)="onGlobalFilter(dt, $event)" placeholder="Search..." />
                    </p-iconfield>
                </div>
            </ng-template>
            <ng-template #header>
                <tr>
                    <th pSortableColumn="name">
                        Name
                        <p-sortIcon field="name" />
                    </th>
                    <th pSortableColumn="status">
                        Status
                        <p-sortIcon field="status" />
                    </th>
                    <th pSortableColumn="systemPromptTemplate">
                        System Prompt
                        <p-sortIcon field="systemPromptTemplate" />
                    </th>
                    <th pSortableColumn="temperature">
                        Temperature
                        <p-sortIcon field="temperature" />
                    </th>
                    <th pSortableColumn="confidenceThreshold">
                        Confidence Threshold
                        <p-sortIcon field="confidenceThreshold" />
                    </th>
                    <th pSortableColumn="strictGuardrails">
                        Strict Guardrails
                        <p-sortIcon field="strictGuardrails" />
                    </th>
                    <th style="min-width: 12rem">Actions</th>
                </tr>
            </ng-template>
            <ng-template #body let-item>
                <tr>
                    <td>{{ item.name }}
                     <span class="block w-48 truncate">{{ item.id }}</span>
                    </td>
                    <td>{{ item.status }}</td>
                    <td>
                        <p-tag styleClass="cursor-pointer" (click)="systemMessage =item.systemPromptTemplate; op.toggle($event)" *ngIf="item.systemPromptTemplate">
                            <span class="block w-48 truncate">{{ item.systemPromptTemplate }}</span>
                        </p-tag>
                        <p-tag icon="pi pi-times-circle" severity="danger" *ngIf="!item.systemPromptTemplate">
                            <span class="">Not provided</span>
                        </p-tag>
                        <p-popover #op [dismissable]="true" appendTo="body">
                            <div class="flex flex-col gap-4 w-[25rem]">
                            <span class="">{{systemMessage}}</span>
                            </div>
                        </p-popover>
                    </td>
                    <td>
                        <p-tag>
                            {{ item.temperature }}
                        </p-tag>
                    </td>
                    <td>
                        <p-tag>
                            {{ item.confidenceThreshold }}
                        </p-tag>
                    </td>
                    <td>
                        <p-tag [icon]="item.strictGuardrails ? 'pi pi-check-circle': 'pi pi-times-circle' "
                               [severity]="item.strictGuardrails ? 'success': 'warn'">
                            {{ item.strictGuardrails ? 'Enabled' : 'Disabled' }}
                        </p-tag>
                    </td>
                    <td class="flex justify-end gap-1">
                        <p-button (onClick)="chatWith(item)" type="button" [icon]="'pi pi-comments'" severity="success"
                                  label="Chat with this bot" />
                        <p-button icon="pi pi-eye" severity="success" label="View" [outlined]="true"
                                  [routerLink]="[routePaths.private.bots.details+item.id]" />
                        <p-button icon="pi pi-pencil" label="Edit" [outlined]="true" (onClick)="openForm(item)" />
                        <p-button icon="pi pi-trash" severity="danger" label="Delete" [outlined]="true"
                                  (onClick)="delete(item)" />

                    </td>
                </tr>
            </ng-template>
            <ng-template pTemplate="emptymessage">
                <tr>
                    <td colspan="6">
                        <div class=" text-center">
                            <app-no-items-found></app-no-items-found>
                        </div>
                    </td>
                </tr>
            </ng-template>
        </p-table>
        <p-confirmdialog [style]="{ width: '450px' }" />
    `,
    providers: [DialogService,MessageService, ConfirmationService]
})
export class BotsComponent implements OnInit {
    items = signal<ChatbotResource[]>([]);
    selectedItems!: ChatbotResource[] | null;
    @ViewChild('dt') dt!: Table;
    totalRecords = signal<number>(0);
    event!: LazyLoadingEventRequest;
     selectedChatBot?: ChatbotResource;
    systemMessage?: String
    protected readonly routePaths = routePaths;

    constructor(
        private localizationService: LocalizationService,
        private dialogService: DialogService,
        private chatBotsService: ChatBotsService,
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
        this.dt.exportCSV();
    }

    ngOnInit() {}

    edit(item: ChatbotResource) {

    }

    delete(item: ChatbotResource) {

    }

    async loadData($event: any) {
        this.event = $event;
        if (this.event) {
            const response = await lastValueFrom(this.chatBotsService.findChatbots({ request: this.event }));
            this.items.set(response.content!);
            this.totalRecords.set(response.totalElements!);
        }
    }

    onGlobalFilter(table: Table, event: Event) {
        table.filterGlobal((event.target as HTMLInputElement).value, 'contains');
    }

    openForm(item?: ChatbotResource) {
        const ref = this.dialogService.open(BotFormDialogComponent, {
            closable: true,
            closeOnEscape: true,
            maximizable: true,
            modal: true,
            maskStyleClass: 'backdrop-blur-sm',
            data: item,
            header: item ? 'Edit Bot' : 'New Bot',
            width: '60vw',
            focusOnShow: false,
            contentStyle: { overflow: 'auto' },
            breakpoints: {
                '640px': '90vw'
            }
        });
        ref.onClose.subscribe((data: ChatbotResource) => {
            if (data) {
                this.items.set(arrayAddOrReplace('id', this.items(), data));
            }
        });
    }

    chatWith(item: ChatbotResource) {
        this.showChat = true;
        this.selectedChatBot = item;
    }
}
