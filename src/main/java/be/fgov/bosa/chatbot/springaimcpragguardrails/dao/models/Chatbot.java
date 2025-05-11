package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.commons.CustomAuditable;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Chatbot extends CustomAuditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ChatbotStatusEnum status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Organization organization;
    
    @OneToMany(mappedBy = "chatbot", cascade = CascadeType.ALL)
    private List<Conversation> conversations = new ArrayList<>();
    
    @OneToMany(mappedBy = "chatbot", cascade = CascadeType.ALL)
    private List<TrainingData> trainingData = new ArrayList<>();

    // New fields for customization
    @Column(length = 10000)
    private String systemPromptTemplate; // Custom system prompt for this bot

    @Column(nullable = false)
    private double temperature = 0.1; // Default temperature

    @Column(nullable = false)
    private double confidenceThreshold = 0.65; // Bot-specific threshold
    @Column(length = 10000)
    private String responseTemplate; // Optional response formatting template

    @Column(nullable = false)
    private boolean strictGuardrails = true; // Enable/disable strict guardrails

    @Column(length = 10000)
    private String customGuardrails; // Bot-specific guardrail rules

}

