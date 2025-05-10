package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ChatbotRepository extends JpaRepository<Chatbot, UUID> {
    List<Chatbot> findByOrganizationId(UUID organizationId);
    @Query("SELECT p FROM Chatbot p " +
            "WHERE ((:value IS NULL) OR (p.name = :value))"
    )
    Page<Chatbot> findItems(String value, Pageable pageRequest);
}
