package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.commons.CustomAuditable;
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
public class Organization extends CustomAuditable<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chatbot> chatbots = new ArrayList<>();
    
}