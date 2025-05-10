package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.commons.CustomAuditable;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.PriorityEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.TrainingDataTypeEnum;
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
public class TrainingData extends CustomAuditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    private TrainingDataTypeEnum type;
    
    @Enumerated(EnumType.STRING)
    private PriorityEnum priority;
    
    @Column(nullable = false)
    private OffsetDateTime contentDate;
    
    @Column(nullable = false)
    private OffsetDateTime addedDate = OffsetDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Chatbot chatbot;
    
    @OneToMany(mappedBy = "trainingData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();
}

