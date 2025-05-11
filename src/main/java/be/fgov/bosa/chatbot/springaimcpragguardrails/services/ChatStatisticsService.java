package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ConversationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.MessageRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.*;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatStatisticsService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Get comprehensive bot statistics
     */
    @Transactional(readOnly = true)
    public BotStatistics getBotStatistics(UUID botId) {
        log.info("Generating statistics for bot: {}", botId);

        // Basic conversation statistics
        ConversationStatistics convStats = conversationRepository.getConversationStatistics(botId);

        // Message statistics
        long totalMessages = messageRepository.countMessagesByBotId(botId);
        long userMessages = messageRepository.countByBotIdAndType(botId, MessageTypeEnum.USER);
        long botMessages = messageRepository.countByBotIdAndType(botId, MessageTypeEnum.BOT);

        // Performance statistics
        Double avgProcessingTime = messageRepository.getAverageProcessingTime(botId);
        Double avgConfidence = messageRepository.getAverageConfidence(botId);

        // Token usage statistics
        Map<String, Integer> tokenUsage = getTokenUsageStatistics(botId);

        // Activity statistics
        Map<String, Long> dailyActivity = getDailyActivityStatistics(botId, 30);
        Map<String, Long> hourlyActivity = getHourlyActivityStatistics(botId, 7);

        // Popular topics/queries
        Map<String, Long> popularTopics = getPopularTopics(botId, 10);

        // User engagement metrics
        UserEngagementMetrics engagement = getUserEngagementMetrics(botId);

        return BotStatistics.builder()
                .botId(botId)
                .totalConversations(convStats.getTotalConversations())
                .activeConversations(convStats.getActiveConversations())
                .avgMessagesPerConversation(convStats.getAvgMessagesPerConversation())
                .totalMessages(totalMessages)
                .userMessages(userMessages)
                .botMessages(botMessages)
                .avgProcessingTime(avgProcessingTime)
                .avgConfidence(avgConfidence)
                .totalInputTokens(tokenUsage.get("input"))
                .totalOutputTokens(tokenUsage.get("output"))
                .dailyActivity(dailyActivity)
                .hourlyActivity(hourlyActivity)
                .popularTopics(popularTopics)
                .averageConversationDuration(engagement.getAvgConversationDuration())
                .userRetentionRate(engagement.getRetentionRate())
                .messageResponseRate(engagement.getResponseRate())
                .lastActivityTime(engagement.getLastActivityTime())
                .build();
    }

    /**
     * Get token usage statistics
     */
    private Map<String, Integer> getTokenUsageStatistics(UUID botId) {
        Integer totalInputTokens = messageRepository.getTotalInputTokensByBotId(botId);
        Integer totalOutputTokens = messageRepository.getTotalOutputTokensByBotId(botId);

        Map<String, Integer> usage = new HashMap<>();
        usage.put("input", totalInputTokens != null ? totalInputTokens : 0);
        usage.put("output", totalOutputTokens != null ? totalOutputTokens : 0);
        usage.put("total", usage.get("input") + usage.get("output"));

        return usage;
    }

    /**
     * Get daily activity statistics for the last N days
     */
    private Map<String, Long> getDailyActivityStatistics(UUID botId, int days) {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        List<DailyActivityStat> stats = messageRepository.getDailyActivityStats(botId, startDate);

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getDate().toString(),
                        DailyActivityStat::getCount
                ));
    }

    /**
     * Get hourly activity statistics for the last N days
     */
    private Map<String, Long> getHourlyActivityStatistics(UUID botId, int days) {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        List<HourlyActivityStat> stats = messageRepository.getHourlyActivityStats(botId, startDate);

        return stats.stream()
                .collect(Collectors.toMap(
                        stat -> stat.getHour().toString(),
                        HourlyActivityStat::getCount
                ));
    }

    /**
     * Get popular topics based on message content
     */
    private Map<String, Long> getPopularTopics(UUID botId, int limit) {
        // This is a simplified implementation - you might want to use more sophisticated
        // text analysis, keyword extraction, or topic modeling
        List<TopicFrequency> topics = messageRepository.getTopicFrequencies(botId, limit);

        return topics.stream()
                .collect(Collectors.toMap(
                        TopicFrequency::getTopic,
                        TopicFrequency::getFrequency
                ));
    }

    /**
     * Get user engagement metrics
     */
    private UserEngagementMetrics getUserEngagementMetrics(UUID botId) {
        // Average conversation duration
        Double avgDuration = 100.000 ;//conversationRepository.getAverageConversationDuration(botId);

        // Retention rate (users who return for multiple conversations)
        Double retentionRate = calculateUserRetentionRate(botId);

        // Response rate (bot messages / user messages)
        long userMsgCount = messageRepository.countByBotIdAndType(botId, MessageTypeEnum.USER);
        long botMsgCount = messageRepository.countByBotIdAndType(botId, MessageTypeEnum.BOT);
        Double responseRate = userMsgCount > 0 ? (double) botMsgCount / userMsgCount : 0.0;

        // Last activity time
        OffsetDateTime lastActivity = conversationRepository.getLastActivityForBot(botId).orElse(null);

        return UserEngagementMetrics.builder()
                .avgConversationDuration(avgDuration)
                .retentionRate(retentionRate)
                .responseRate(responseRate)
                .lastActivityTime(lastActivity)
                .build();
    }

    /**
     * Calculate user retention rate
     */
    private Double calculateUserRetentionRate(UUID botId) {
        // This is a simplified calculation - you might want to refine based on your business logic
        List<String> userIds = new ArrayList<>();
        List<String> returningUsers =  new ArrayList<>();

        if (userIds.isEmpty()) {
            return 0.0;
        }

        return (double) returningUsers.size() / userIds.size() * 100;
    }

    /**
     * Get conversation summary for a specific date range
     */
    @Transactional(readOnly = true)
    public ConversationSummaryReport getConversationSummaryReport(UUID botId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Conversation> conversations = conversationRepository.findByChatbotIdAndDateRange(botId, startDate, endDate);

        long totalConversations = conversations.size();
        long completedConversations = conversations.stream()
                .filter(c -> c.getEndTime() != null)
                .count();

        long totalMessages = conversations.stream()
                .mapToLong(Conversation::getTotalMessages)
                .sum();

        double avgMessagesPerConv = totalConversations > 0 ? (double) totalMessages / totalConversations : 0.0;

        // Calculate peak hours
        Map<Integer, Long> hourlyDistribution = getHourlyMessageDistribution(botId, startDate, endDate);
        Integer peakHour = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        return ConversationSummaryReport.builder()
                .botId(botId)
                .startDate(startDate)
                .endDate(endDate)
                .totalConversations(totalConversations)
                .completedConversations(completedConversations)
                .activeConversations(totalConversations - completedConversations)
                .totalMessages(totalMessages)
                .avgMessagesPerConversation(avgMessagesPerConv)
                .peakActiveHour(peakHour)
                .hourlyDistribution(hourlyDistribution)
                .build();
    }

    /**
     * Get hourly message distribution
     */
    private Map<Integer, Long> getHourlyMessageDistribution(UUID botId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<HourlyMessageCount> hourlyData = messageRepository.getHourlyMessageDistribution(botId, startDate, endDate);

        return hourlyData.stream()
                .collect(Collectors.toMap(
                        HourlyMessageCount::getHour,
                        HourlyMessageCount::getCount
                ));
    }

    /**
     * Export statistics to CSV format
     */
    public String exportStatisticsToCsv(UUID botId, OffsetDateTime startDate, OffsetDateTime endDate) {
        BotStatistics stats = getBotStatistics(botId);

        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append("Bot ID,").append(botId).append("\n");
        csv.append("Total Conversations,").append(stats.getTotalConversations()).append("\n");
        csv.append("Active Conversations,").append(stats.getActiveConversations()).append("\n");
        csv.append("Total Messages,").append(stats.getTotalMessages()).append("\n");
        csv.append("Average Processing Time (ms),").append(stats.getAvgProcessingTime()).append("\n");
        csv.append("Average Confidence,").append(stats.getAvgConfidence()).append("\n");
        csv.append("Total Input Tokens,").append(stats.getTotalInputTokens()).append("\n");
        csv.append("Total Output Tokens,").append(stats.getTotalOutputTokens()).append("\n");
        csv.append("User Retention Rate,").append(stats.getUserRetentionRate()).append("%\n");

        // Add daily activity
        csv.append("\nDaily Activity\n");
        csv.append("Date,Message Count\n");
        stats.getDailyActivity().forEach((date, count) ->
                csv.append(date).append(",").append(count).append("\n"));

        return csv.toString();
    }
}

