import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DialogService, DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { LocalizationService } from '../../@core/i18n/localization.service';
import { SharedToasterService } from '../../@core/services/shared.toaster.service';
import { AutoComplete, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { lastValueFrom } from 'rxjs';
import { OrganisationsService } from '../../@core/api/generated/services/organisations.service';
import { ChatbotResource } from '../../@core/api/generated/models/chatbot-resource';
import { OrganizationResource } from '../../@core/api/generated/models/organization-resource';
import { ChatBotsService } from '../../@core/api/generated/services/chat-bots.service';
import { FloatLabel } from 'primeng/floatlabel';
import { Textarea } from 'primeng/textarea';
import { InputNumber } from 'primeng/inputnumber';
import { IftaLabel } from 'primeng/iftalabel';

@Component({
    selector: 'app-bot-form',
    template: `
        <form [formGroup]="form" (ngSubmit)="onSubmit()" class="p-fluid space-y-4">
            <div class="grid grid-cols-12 gap-4">
                <div class="col-span-12 xl:col-span-4">
                    <p-floatlabel variant="in">
                        <label for="name" class="block text-sm font-medium">Name</label>
                        <input fluid id="name" type="text" pInputText formControlName="name" />
                    </p-floatlabel>
                </div>
                <div class="col-span-12 xl:col-span-4">
                    <p-floatlabel variant="in">
                        <label for="description">Description</label>
                        <input fluid id="description" type="text" pInputText formControlName="description" />
                    </p-floatlabel>
                </div>
                <div class="col-span-12 xl:col-span-4">
                    <p-floatlabel variant="in">
                        <p-auto-complete
                            dataKey="id"
                            [forceSelection]="true"
                            [showClear]="true"
                            fluid
                            appendTo="body"
                            formControlName="organization"
                            optionLabel="name"
                            dropdown
                            (completeMethod)="autocomplete($event)"
                            [suggestions]="organisations()"
                        />
                        <label for="organization" class="block font-bold mb-3">Select an organization</label>
                    </p-floatlabel>
                </div>
                <div class="col-span-12 xl:col-span-6">
                    <p-floatlabel variant="in">
                        <p-input-number
                            [showButtons]="true"
                            decrementButtonClass="p-button-text"
                            incrementButtonClass="p-button-text"
                            fluid
                            autocomplete="off"
                            formControlName="confidenceThreshold"
                            class="w-full"
                            [step]="0.01"
                            [min]="0.01"
                            [max]="1"
                            mode="decimal"
                            inputId="confidenceThreshold"
                        >
                        </p-input-number>
                        <label class=" flex items-center gap-1" for="doctor">
                            Confidence Threshold</label
                        >
                    </p-floatlabel>
                </div>
                <div class="col-span-12 xl:col-span-6">
                    <p-floatlabel variant="in">
                        <p-input-number
                            [showButtons]="true"
                            decrementButtonClass="p-button-text"
                            incrementButtonClass="p-button-text"
                            fluid
                            autocomplete="off"
                            formControlName="temperature"
                            class="w-full"
                            mode="decimal"
                            [step]="0.01"
                            [min]="0.01"
                            [max]="1"
                            inputId="temperature"
                        >
                        </p-input-number>
                        <label class=" flex items-center gap-1" for="doctor"> Temperature</label>
                    </p-floatlabel>
                </div>

                <div class="col-span-12 xl:col-span-12">
                    <p-iftalabel >
                        <label for="systemPromptTemplate">System Prompt Template</label>
                        <textarea pTextarea rows="3" fluid id="systemPromptTemplate" type="text"  formControlName="systemPromptTemplate" ></textarea>
                    </p-iftalabel>
                </div>
                <div class="col-span-12 xl:col-span-12">
                    <p-iftalabel >
                        <label for="responseTemplate">Response Template</label>
                        <textarea pTextarea rows="3" fluid id="responseTemplate" type="text"  formControlName="responseTemplate" ></textarea>
                    </p-iftalabel>
                </div>
                <div class="col-span-12 xl:col-span-12">
                    <p-iftalabel >
                        <label for="customGuardrails">Custom Guardrails</label>
                        <input fluid id="customGuardrails" type="text" pInputText formControlName="customGuardrails" />
                    </p-iftalabel>
                </div>
            </div>

            <div class="flex justify-end gap-2">
                <p-button type="button" icon="pi pi-undo" label="Cancel" severity="secondary" (click)="onCancel()"></p-button>
                <p-button type="submit" icon="pi pi-save" label="Submit" [disabled]="form.invalid"></p-button>
            </div>
        </form>
    `,
    imports: [ReactiveFormsModule, InputTextModule, ButtonModule, ToggleButtonModule, FloatLabel, Textarea, InputNumber, AutoComplete, IftaLabel],
    providers: [DialogService, MessageService]
})
export class BotFormDialogComponent implements OnInit {
    form!: FormGroup;
    organisations = signal<OrganizationResource[]>([]);
    chatbot?: ChatbotResource;
    constructor(
        private ref: DynamicDialogRef,
        private config: DynamicDialogConfig,
        private sharedToasterService: SharedToasterService,
        private localizationService: LocalizationService,
        private fb: FormBuilder,
        private chatBotsService: ChatBotsService,
        private organisationsService: OrganisationsService
    ) {
        this.chatbot = config.data;
    }

    get name() {
        return this.form.get('name');
    }

    get description() {
        return this.form.get('description');
    }

    get status() {
        return this.form.get('status');
    }

    get organization() {
        return this.form.get('organization');
    }

    get confidenceThreshold() {
        return this.form.get('confidenceThreshold');
    }

    get temperature() {
        return this.form.get('temperature');
    }

    get systemPromptTemplate() {
        return this.form.get('systemPromptTemplate');
    }

    get responseTemplate() {
        return this.form.get('responseTemplate');
    }

    get strictGuardrails() {
        return this.form.get('strictGuardrails');
    }

    get id() {
        return this.form.controls['id'];
    }

    get customGuardrails() {
        return this.form.get('customGuardrails');
    }
    async autocomplete($event: AutoCompleteCompleteEvent) {
        const query = $event.query?.trim();

        this.organisations.set(await lastValueFrom(this.organisationsService.searchOrganisations({ query: query })));
    }
    ngOnInit(): void {
        this.form = this.fb.group({
            id: [undefined],
            name:[undefined, Validators.required],
            description: [undefined],
            status: [undefined],
            organization: [undefined, Validators.required],
            confidenceThreshold:[undefined],
            temperature: [undefined],
            systemPromptTemplate: [undefined],
            responseTemplate:[undefined],
            strictGuardrails: [undefined],
            customGuardrails: [undefined],
        });
        this.applyDataToForm(this.chatbot);
    }

    applyDataToForm(item?: ChatbotResource): void {
        if (item) {
            this.form.patchValue({
                id: item.id,
                name: item.name,
                description: item.description,
                status: item.status,
                organization: item.organization,
                confidenceThreshold: item.confidenceThreshold,
                temperature: item.temperature,
                systemPromptTemplate: item.systemPromptTemplate,
                responseTemplate: item.responseTemplate,
                strictGuardrails: item.strictGuardrails,
                customGuardrails: item.customGuardrails,
            });
        }
    }

    onSubmit(): void {
        this.form.markAllAsTouched();
        if (this.form.valid) {
            const model: ChatbotResource = {
                ...this.chatbot,
                id: this.id?.value,
                name: this.name?.value,
                description: this.description?.value,
                status: this.status?.value,
                organization: this.organization?.value,
                confidenceThreshold: this.confidenceThreshold?.value,
                temperature: this.temperature?.value,
                systemPromptTemplate: this.systemPromptTemplate?.value,
                responseTemplate: this.responseTemplate?.value,
                strictGuardrails: this.strictGuardrails?.value,
                customGuardrails: this.customGuardrails?.value,

            };
                this.chatBotsService.saveChatbot({ body: model }).subscribe((response) => {
                    if (response) {
                        this.sharedToasterService.actionSuccess.next('patient.created');
                        this.onCancel(response);
                    }
                });
        }
    }

    onCancel(item?: ChatbotResource): void {
        this.ref.close(item);
        // Logic to close the dialog or reset the form
    }
}
