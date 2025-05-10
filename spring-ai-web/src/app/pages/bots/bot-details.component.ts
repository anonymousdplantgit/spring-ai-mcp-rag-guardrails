import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { lastValueFrom } from 'rxjs';
import { TabsModule } from 'primeng/tabs';

import { ChatBotsService } from '../../@core/api/generated/services/chat-bots.service';
import { ChatbotResource } from '../../@core/api/generated/models/chatbot-resource';

@Component({
    selector: 'app-bot-details',
    standalone: true,
    imports: [TabsModule],
    template: `
        <h1>{{ chatbot?.name }}</h1>
        <p-tabs value="0" [scrollable]="true">
            <p-tablist>
                <p-tab value="0"><i class="pi pi-database"></i>Training data</p-tab>
                <p-tab value="1"><i class="pi pi-comments"></i>Conversations</p-tab>
            </p-tablist>
            <p-tabpanels>
                <p-tabpanel value="0">
                    <p class="m-0">
                        At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa
                        qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus.
                    </p>
                </p-tabpanel>
                <p-tabpanel value="1">
                    <p class="m-0">
                        At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa
                        qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus.
                    </p>
                </p-tabpanel>
            </p-tabpanels>
        </p-tabs>
    `
})
export class BotDetailsComponent implements OnInit {
    chatbot?: ChatbotResource;

    private readonly chatbotIdParam: string | null;

    constructor(
        private activatedRoute: ActivatedRoute,
        private chatBotsService: ChatBotsService
    ) {
        this.chatbotIdParam = this.activatedRoute.snapshot.paramMap.get('chatbotId');
    }

    async ngOnInit() {
        if (this.chatbotIdParam) {
            this.chatbot = await lastValueFrom(this.chatBotsService.getChatbot({ id: this.chatbotIdParam }));
        }
    }
}
