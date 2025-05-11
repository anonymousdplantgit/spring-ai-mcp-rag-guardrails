package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("SELECT c FROM Conversation c " +
            "WHERE (:value IS NULL OR lower(c.chatbot.name) LIKE lower(concat('%', cast(:value as string), '%')))")
    Page<Conversation> autocomplete(String value, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "WHERE (:value IS NULL OR lower(c.chatbot.name) LIKE lower(concat('%', cast(:value as string), '%')))")
    Page<Conversation> findItems(String value, Pageable pageable);
}
