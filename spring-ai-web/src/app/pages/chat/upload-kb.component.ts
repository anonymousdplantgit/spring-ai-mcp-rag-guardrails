import { AfterViewChecked, Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FileUpload } from 'primeng/fileupload';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { lastValueFrom } from 'rxjs';
import { ChatbotResource } from '../../@core/api/generated/models/chatbot-resource';
import { ChatBotsService } from '../../@core/api/generated/services/chat-bots.service';

@Component({
    selector: 'app-upload-kb',
    template: `
        <p-button icon="pi pi-trash" severity="danger" label="Clear bot knowledge base" fluid styleClass="mb-2" (onClick)="onClearBotKnowledgeBase()"></p-button>
        <p-fileupload name="document" [multiple]="false" accept=".pdf,.doc,.docx,.html,.txt" [maxFileSize]="10000000" (uploadHandler)="onUpload($event)" [customUpload]="true" chooseLabel="Select A knowledge base document" uploadLabel="Upload">
        </p-fileupload>
    `,
    imports: [FormsModule, FileUpload, Button]
})
export class UploadKbComponent implements OnInit, AfterViewChecked {
    @Input() chatBot?: ChatbotResource;
    constructor(
        private messageService: MessageService,
        private chatBotsService: ChatBotsService
    ) {}

    ngOnInit() {}

    ngAfterViewChecked() {}

    onUpload(event: any) {
        const files = event.files;
        if (files && files.length > 0) {
            this.uploadDocument(files[0]);
        }
    }

    uploadDocument(file: File) {
        if(this.chatBot){
            this.chatBotsService
                .uploadDocument({
                    botId: this.chatBot?.id!,
                    body: {
                        file: file,
                    }
                })
                .subscribe({
                    next: (response) => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Success',
                            detail: 'Document uploaded successfully'
                        });
                    },
                    error: (error) => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Error',
                            detail: 'Failed to upload document: ' + (error.message || 'Unknown error')
                        });
                    }
                });
        }

    }

    async onClearBotKnowledgeBase() {
        if(this.chatBot){
        const response = await lastValueFrom(this.chatBotsService.clearBotKnowledgeBase({
            botId:this.chatBot.id!,
        }))
        this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Documents cleared successfully'
        });
    }}
}
