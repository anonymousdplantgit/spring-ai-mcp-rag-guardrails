package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationStatistics;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Find conversations by bot ID
     */
    List<Conversation> findByChatbotId(UUID chatbotId);

    /**
     * Find conversations by bot ID with pagination
     */
    Page<Conversation> findByChatbotId(UUID chatbotId, Pageable pageable);

    /**
     * Find conversations by status
     */
    List<Conversation> findByStatus(ConversationStatusEnum status);

    /**
     * Find active conversations (not ended)
     */
    @Query("SELECT c FROM Conversation c WHERE c.endTime IS NULL ORDER BY c.lastActivity DESC")
    List<Conversation> findActiveConversations();

    /**
     * Find conversations for a bot by status
     */
    @Query("SELECT c FROM Conversation c WHERE c.chatbot.id = :botId AND c.status = :status " +
            "ORDER BY c.lastActivity DESC")
    List<Conversation> findByChatbotIdAndStatus(@Param("botId") UUID botId, @Param("status") ConversationStatusEnum status);

    /**
     * Find recent conversations for a bot
     */
    @Query("SELECT c FROM Conversation c WHERE c.chatbot.id = :botId " +
            "ORDER BY c.lastActivity DESC")
    List<Conversation> findRecentConversationsByBotId(@Param("botId") UUID botId, Pageable pageable);

    /**
     * Count conversations for a bot
     */
    long countByChatbotId(UUID chatbotId);

    /**
     * Count active conversations for a bot
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.chatbot.id = :botId AND c.endTime IS NULL")
    long countActiveConversationsByBotId(@Param("botId") UUID botId);

    /**
     * Find conversations by date range
     */
    @Query("SELECT c FROM Conversation c WHERE c.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY c.startTime DESC")
    List<Conversation> findByDateRange(@Param("startDate") OffsetDateTime startDate,
                                       @Param("endDate") OffsetDateTime endDate);

    /**
     * Find conversations by bot and date range
     */
    @Query("SELECT c FROM Conversation c WHERE c.chatbot.id = :botId " +
            "AND c.startTime BETWEEN :startDate AND :endDate " +
            "ORDER BY c.startTime DESC")
    List<Conversation> findByChatbotIdAndDateRange(@Param("botId") UUID botId,
                                                   @Param("startDate") OffsetDateTime startDate,
                                                   @Param("endDate") OffsetDateTime endDate);

    /**
     * Find conversations with more than X messages
     */
    @Query("SELECT c FROM Conversation c WHERE c.totalMessages > :threshold " +
            "ORDER BY c.totalMessages DESC")
    List<Conversation> findConversationsWithMoreThanXMessages(@Param("threshold") Integer threshold);

    /**
     * Find stale conversations (no activity for X hours)
     */
    @Query("SELECT c FROM Conversation c WHERE c.lastActivity < :threshold AND c.endTime IS NULL")
    List<Conversation> findStaleConversations(@Param("threshold") OffsetDateTime threshold);

    /**
     * Get conversation statistics for a bot
     */
    @Query("SELECT " +
            "COUNT(c) as totalConversations, " +
            "AVG(c.totalMessages) as avgMessagesPerConversation, " +
            "COUNT(CASE WHEN c.endTime IS NULL THEN 1 END) as activeConversations " +
            "FROM Conversation c WHERE c.chatbot.id = :botId")
    ConversationStatistics getConversationStatistics(@Param("botId") UUID botId);

    /**
     * Get average conversation duration
     */
/*    @Query("SELECT AVG(EXTRACT(EPOCH FROM (c.endTime - c.startTime))) " +
            "FROM Conversation c WHERE c.chatbot.id = :botId AND c.endTime IS NOT NULL")
    Double getAverageConversationDuration(@Param("botId") UUID botId);*/

    /**
     * Autocomplete search
     */
    @Query("SELECT c FROM Conversation c " +
            "WHERE (:value IS NULL OR c.id = :value)")
    Page<Conversation> autocomplete(String value, Pageable pageable);

    /**
     * Find items with filtering
     */
    @Query("SELECT c FROM Conversation c " +
            "WHERE (:value IS NULL OR c.id = :value)")
    Page<Conversation> findItems(String value, Pageable pageable);

    /**
     * Get last activity for a bot
     */
    @Query("SELECT MAX(c.lastActivity) FROM Conversation c WHERE c.chatbot.id = :botId")
    Optional<OffsetDateTime> getLastActivityForBot(@Param("botId") UUID botId);
}

