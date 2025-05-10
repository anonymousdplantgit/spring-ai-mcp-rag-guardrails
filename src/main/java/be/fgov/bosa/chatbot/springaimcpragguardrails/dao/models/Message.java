package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.commons.CustomAuditable;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message extends CustomAuditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Conversation conversation;
    
    @Column(nullable = false)
    private String content;
    
    @Column(nullable = false)
    private OffsetDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum type;
    
    @ManyToMany
    @JoinTable(
        name = "message_resources",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private List<Resource> resources = new ArrayList<>();
}

