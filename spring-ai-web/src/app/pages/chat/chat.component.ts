import { AfterViewChecked, Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { lastValueFrom } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { DecimalPipe, NgClass, NgForOf, NgIf } from '@angular/common';
import { ProgressSpinner } from 'primeng/progressspinner';
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';
import { Divider } from 'primeng/divider';
import { MessageService } from 'primeng/api';
import { UploadKbComponent } from './upload-kb.component';
import { OverlayBadge } from 'primeng/overlaybadge';
import { ChatbotResource } from '../../@core/api/generated/models/chatbot-resource';
import { ChatBotsService } from '../../@core/api/generated/services/chat-bots.service';

@Component({
    selector: 'app-chat',
    template: `
        <div class="flex flex-col gap-2 h-full surface-hover">
            <app-upload-kb [chatBot]="selectedChatBot"></app-upload-kb>
            <p-divider></p-divider>
            <!-- Chat Messages Area with flex-grow to take available space -->
            <div #scrollMe class=" rounded-lg flex-grow overflow-y-auto px-4 py-2" style="scroll-behavior: smooth;">
                <!-- Welcome message -->
                <div *ngIf="messages.length === 0" class="flex justify-center items-center h-full">
                    <div class="text-center text-gray-500">
                        <i class="pi pi-comments text-4xl mb-2" style="font-size:xx-large"> </i>
                        <p class="text-xl">Start a conversation</p>
                    </div>
                </div>

                <!-- Message bubbles -->
                <div *ngFor="let msg of messages" class="mb-4">
                    <div class="flex" [ngClass]="msg.isUser ? 'justify-end' : 'justify-start'">
                        <!-- Bot avatar for non-user messages -->
                        <div *ngIf="!msg.isUser" class="flex-shrink-0 mr-2">
                            <div class="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center">
                                <p-overlay-badge value="{{msg.confidence | number}}" [severity]="msg.confidence! > 0.8 ? 'success' : msg.confidence! > 0.5 ? 'warn' : 'danger'">
                                    <i class="pi pi-android text-blue-500" style="font-size: xx-large"></i>
                                </p-overlay-badge>
                            </div>
                        </div>

                        <!-- Message bubble -->
                        <div
                            [innerText]="msg.text"
                            [ngClass]="msg.isUser ? 'bg-blue-500 text-white rounded-tl-lg rounded-tr-lg rounded-bl-lg' : 'bg-gray-100 text-gray-800 rounded-tl-lg rounded-tr-lg rounded-br-lg'"
                            class="px-3 py-2 max-w-[75%] break-words shadow-sm"
                        ></div>

                        <!-- User avatar for user messages -->
                        <div *ngIf="msg.isUser" class="flex-shrink-0 ml-2">
                            <div class="w-14 h-14 rounded-full bg-blue-500 flex items-center justify-center">
                                <i class="pi pi-user text-white" style="font-size: xx-large"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Loading indicator -->
                <div *ngIf="isLoading" class="flex justify-start mb-4">
                    <div class="flex-shrink-0 mr-2">
                        <div class="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center">
                            <i class="pi pi-android text-blue-500" style="font-size: xx-large"></i>
                        </div>
                    </div>
                    <div class="bg-gray-100 text-gray-800 rounded-tl-lg rounded-tr-lg rounded-br-lg px-4 py-2 flex items-center">
                        <p-progressSpinner strokeWidth="2" fill="transparent" animationDuration=".5s"></p-progressSpinner>
                        <span class="ml-2">Typing...</span>
                    </div>
                </div>
            </div>

            <!-- Input area fixed at bottom -->
            <div class="border-t border-gray-200 p-3 surface-hover">
                <div class="flex items-center gap-2">
                    <div class="flex-grow">
                        <input
                            pInputText
                            class="w-full p-inputtext p-2 border rounded-lg focus:ring-2 focus:ring-blue-300 focus:border-blue-500"
                            [(ngModel)]="userInput"
                            placeholder="Type a message..."
                            (keyup.enter)="submitV2()"
                            [disabled]="isLoading"
                        />
                    </div>
                    <p-button icon="pi pi-send" [disabled]="!selectedChatBot || !userInput.trim() || isLoading" (click)="submitV2()"></p-button>
                </div>
            </div>
        </div>
    `,
    imports: [FormsModule, NgClass, ProgressSpinner, InputText, NgIf, NgForOf, Divider, Button, UploadKbComponent, OverlayBadge, DecimalPipe]
})
export class ChatComponent implements OnInit, AfterViewChecked {
    messages: Array<{ text: string; isUser: boolean,retrievalConfidence?: number; confidence?: number }> = [];
    userInput: string = '';
    isLoading: boolean = false;
    conversationId?: string;
    bots: ChatbotResource[] = [];
    @Input() selectedChatBot!: ChatbotResource | undefined;
    @ViewChild('scrollMe') private messagesContainer!: ElementRef;

    constructor(
        private messageService: MessageService,
        private chatControllerService: ChatBotsService,
    private chatBotsService: ChatBotsService
    ) {}

    ngOnInit() {
        this.loadBots()
    }

    async loadBots() {
        this.bots = await lastValueFrom(this.chatBotsService.getAllChatbots())
    }

    ngAfterViewChecked() {
        this.scrollToBottom();
    }

    scrollToBottom(): void {
        try {
            this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
        } catch (err) {}
    }

    async submitV2() {
        if (!this.userInput.trim() || !this.selectedChatBot) {
            return;
        }
        // Add user message
        const userMessage = this.userInput.trim();
        this.messages.push({
            text: userMessage,
            isUser: true
        });
        // Clear input and show loading
        this.userInput = '';
        this.isLoading = true;
        try {
            // Call API
            const chatResponse = await lastValueFrom(
                this.chatBotsService.ragChat({
                    body: {
                        message: userMessage,
                        conversationId: this.conversationId,
                        botId: this.selectedChatBot.id!
                    }
                })
            );
            console.log(chatResponse);
            this.conversationId = chatResponse.conversationId;
            // Add bot response
            if (chatResponse) {
                this.messages.push({
                    text: chatResponse.response!,
                    isUser: false,
                    confidence: chatResponse.confidence,
                    retrievalConfidence: chatResponse.retrievalConfidence,
                });
            }
        } catch (error) {
            console.error('Error sending message:', error);
            this.messages.push({
                text: "Sorry, I couldn't process your message. Please try again.",
                isUser: false
            });
        } finally {
            this.isLoading = false;
        }
    }
}
