package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.commons.CustomAuditable;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationReviewStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Conversation extends CustomAuditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Chatbot chatbot;

    @Enumerated(EnumType.STRING)
    private ConversationStatusEnum status;
    @Enumerated(EnumType.STRING)
    private ConversationReviewStatusEnum reviewStatus;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();
    
    @Column(nullable = false)
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime lastActivity = OffsetDateTime.now();
    private Integer totalMessages = 0;

    // Helper method to add a message
    public void addMessage(Message message) {
        if(messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
        message.setConversation(this);
        this.totalMessages = messages.size();
        this.lastActivity = OffsetDateTime.now();
    }

    // Helper method to end the conversation
    public void endConversation() {
        this.endTime = OffsetDateTime.now();
        this.reviewStatus = ConversationReviewStatusEnum.REVIEWED;
    }

}