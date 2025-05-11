package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Message;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.DailyActivityStat;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.HourlyActivityStat;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.HourlyMessageCount;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.TopicFrequency;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Find messages by conversation ID ordered by timestamp
     */
    List<Message> findByConversationIdOrderByTimestampAsc(UUID conversationId);

    /**
     * Find messages by conversation ID with pagination
     */
    Page<Message> findByConversationIdOrderByTimestampAsc(UUID conversationId, Pageable pageable);

    /**
     * Find recent messages by conversation ID
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.timestamp DESC")
    List<Message> findRecentMessagesByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    /**
     * Count messages by type for a conversation
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.type = :type")
    long countByConversationIdAndType(@Param("conversationId") UUID conversationId, @Param("type") MessageTypeEnum type);

    /**
     * Count messages by bot ID and type
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.type = :type")
    long countByBotIdAndType(@Param("botId") UUID botId, @Param("type") MessageTypeEnum type);

    /**
     * Find messages by bot ID
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "ORDER BY m.timestamp DESC")
    List<Message> findByBotId(@Param("botId") UUID botId, Pageable pageable);

    /**
     * Count total messages for a bot
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.chatbot.id = :botId")
    long countMessagesByBotId(@Param("botId") UUID botId);

    /**
     * Find recent messages for a bot
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "ORDER BY m.timestamp DESC")
    List<Message> findRecentMessagesByBotId(@Param("botId") UUID botId, Pageable pageable);

    /**
     * Find messages by date range
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY m.timestamp ASC")
    List<Message> findByConversationIdAndDateRange(
            @Param("conversationId") UUID conversationId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    /**
     * Find messages with high confidence scores
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.confidence > :threshold AND m.type = 'BOT' " +
            "ORDER BY m.confidence DESC")
    List<Message> findHighConfidenceMessages(@Param("botId") UUID botId, @Param("threshold") Double threshold);

    /**
     * Get average processing time for bot responses
     */
    @Query("SELECT AVG(m.processingTimeMs) FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.type = 'BOT' AND m.processingTimeMs IS NOT NULL")
    Double getAverageProcessingTime(@Param("botId") UUID botId);

    /**
     * Get average confidence for bot responses
     */
    @Query("SELECT AVG(m.confidence) FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.type = 'BOT' AND m.confidence IS NOT NULL")
    Double getAverageConfidence(@Param("botId") UUID botId);

    /**
     * Get total token usage for a conversation
     */
    @Query("SELECT COALESCE(SUM(m.inputTokens), 0) + COALESCE(SUM(m.outputTokens), 0) " +
            "FROM Message m WHERE m.conversation.id = :conversationId")
    Integer getTotalTokenUsage(@Param("conversationId") UUID conversationId);

    /**
     * Get total input tokens for a bot
     */
    @Query("SELECT COALESCE(SUM(m.inputTokens), 0) FROM Message m " +
            "WHERE m.conversation.chatbot.id = :botId AND m.inputTokens IS NOT NULL")
    Integer getTotalInputTokensByBotId(@Param("botId") UUID botId);

    /**
     * Get total output tokens for a bot
     */
    @Query("SELECT COALESCE(SUM(m.outputTokens), 0) FROM Message m " +
            "WHERE m.conversation.chatbot.id = :botId AND m.outputTokens IS NOT NULL")
    Integer getTotalOutputTokensByBotId(@Param("botId") UUID botId);

    /**
     * Find messages containing specific text
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "ORDER BY m.timestamp ASC")
    List<Message> searchMessagesInConversation(
            @Param("conversationId") UUID conversationId,
            @Param("searchText") String searchText);

    /**
     * Global message search across all conversations
     */
    @Query("SELECT m FROM Message m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "ORDER BY m.timestamp DESC")
    Page<Message> searchMessagesGlobal(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Delete messages older than a specific date
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.timestamp < :date")
    void deleteMessagesOlderThan(@Param("date") OffsetDateTime date);

    /**
     * Find conversations with messages containing specific keywords
     */
    @Query("SELECT DISTINCT m.conversation.id FROM Message m " +
            "WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UUID> findConversationsWithKeyword(@Param("keyword") String keyword);

    /**
     * Get daily activity statistics
     */
    @Query("SELECT CAST(m.timestamp AS DATE) as date, COUNT(m) as count " +
            "FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.timestamp >= :startDate " +
            "GROUP BY CAST(m.timestamp AS DATE) " +
            "ORDER BY CAST(m.timestamp AS DATE)")
    List<DailyActivityStat> getDailyActivityStats(@Param("botId") UUID botId, @Param("startDate") OffsetDateTime startDate);

    /**
     * Get hourly activity statistics
     */
    @Query("SELECT EXTRACT(HOUR FROM m.timestamp) as hour, COUNT(m) as count " +
            "FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.timestamp >= :startDate " +
            "GROUP BY EXTRACT(HOUR FROM m.timestamp) " +
            "ORDER BY EXTRACT(HOUR FROM m.timestamp)")
    List<HourlyActivityStat> getHourlyActivityStats(@Param("botId") UUID botId, @Param("startDate") OffsetDateTime startDate);

    /**
     * Get topic frequencies (simplified)
     */
    @Query(value = "SELECT regexp_split_to_table(LOWER(content), '\\s+') as topic, COUNT(*) as frequency " +
            "FROM messages m " +
            "JOIN conversations c ON m.conversation_id = c.id " +
            "WHERE c.chatbot_id = :botId " +
            "AND m.type = 'USER' " +
            "AND LENGTH(regexp_split_to_table(LOWER(content), '\\s+')) > 3 " +
            "GROUP BY topic " +
            "ORDER BY frequency DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<TopicFrequency> getTopicFrequencies(@Param("botId") UUID botId, @Param("limit") int limit);

    /**
     * Get hourly message distribution
     */
    @Query("SELECT EXTRACT(HOUR FROM m.timestamp) as hour, COUNT(m) as count " +
            "FROM Message m WHERE m.conversation.chatbot.id = :botId " +
            "AND m.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(HOUR FROM m.timestamp) " +
            "ORDER BY EXTRACT(HOUR FROM m.timestamp)")
    List<HourlyMessageCount> getHourlyMessageDistribution(
            @Param("botId") UUID botId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);
}

