package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.BotStatistics;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationSummaryReport;
import be.fgov.bosa.chatbot.springaimcpragguardrails.services.ChatStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistics", description = "Analytics and statistics for chatbots")
public class StatisticsController {

    private final ChatStatisticsService statisticsService;

    @GetMapping("/bot/{botId}")
    @Operation(summary = "Get bot statistics", description = "Get comprehensive statistics for a specific bot")
    public ResponseEntity<BotStatistics> getBotStatistics(
            @PathVariable @Parameter(description = "Bot ID") UUID botId) {
        try {
            BotStatistics statistics = statisticsService.getBotStatistics(botId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting statistics for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/summary")
    @Operation(summary = "Get conversation summary", description = "Get conversation summary for a date range")
    public ResponseEntity<ConversationSummaryReport> getConversationSummary(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam @Parameter(description = "Start date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @Parameter(description = "End date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        try {
            ConversationSummaryReport summary = statisticsService.getConversationSummaryReport(botId, startDate, endDate);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting conversation summary for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/export")
    @Operation(summary = "Export statistics", description = "Export bot statistics to CSV format")
    public ResponseEntity<String> exportStatistics(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam @Parameter(description = "Start date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @Parameter(description = "End date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        try {
            String csvData = statisticsService.exportStatisticsToCsv(botId, startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bot_statistics_" + botId + ".csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            log.error("Error exporting statistics for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/activity/daily")
    @Operation(summary = "Get daily activity", description = "Get daily message activity for a bot")
    public ResponseEntity<Map<String, Long>> getDailyActivity(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam(defaultValue = "30") @Parameter(description = "Number of days") int days) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Long> dailyActivity = stats.getDailyActivity();
            return ResponseEntity.ok(dailyActivity);
        } catch (Exception e) {
            log.error("Error getting daily activity for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/activity/hourly")
    @Operation(summary = "Get hourly activity", description = "Get hourly message activity for a bot")
    public ResponseEntity<Map<String, Long>> getHourlyActivity(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam(defaultValue = "7") @Parameter(description = "Number of days") int days) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Long> hourlyActivity = stats.getHourlyActivity();
            return ResponseEntity.ok(hourlyActivity);
        } catch (Exception e) {
            log.error("Error getting hourly activity for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/topics")
    @Operation(summary = "Get popular topics", description = "Get most discussed topics for a bot")
    public ResponseEntity<Map<String, Long>> getPopularTopics(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam(defaultValue = "10") @Parameter(description = "Number of topics") int limit) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Long> popularTopics = stats.getPopularTopics();
            return ResponseEntity.ok(popularTopics);
        } catch (Exception e) {
            log.error("Error getting popular topics for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/tokens")
    @Operation(summary = "Get token usage", description = "Get token usage statistics for a bot")
    public ResponseEntity<Map<String, Integer>> getTokenUsage(
            @PathVariable @Parameter(description = "Bot ID") UUID botId) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Integer> tokenUsage = Map.of(
                    "totalInputTokens", stats.getTotalInputTokens(),
                    "totalOutputTokens", stats.getTotalOutputTokens(),
                    "totalTokens", stats.getTotalInputTokens() + stats.getTotalOutputTokens()
            );
            return ResponseEntity.ok(tokenUsage);
        } catch (Exception e) {
            log.error("Error getting token usage for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/performance")
    @Operation(summary = "Get performance metrics", description = "Get performance metrics for a bot")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @PathVariable @Parameter(description = "Bot ID") UUID botId) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Object> performance = Map.of(
                    "averageProcessingTime", stats.getAvgProcessingTime(),
                    "averageConfidence", stats.getAvgConfidence(),
                    "messageResponseRate", stats.getMessageResponseRate(),
                    "averageConversationDuration", stats.getAverageConversationDuration()
            );
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            log.error("Error getting performance metrics for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/engagement")
    @Operation(summary = "Get engagement metrics", description = "Get user engagement metrics for a bot")
    public ResponseEntity<Map<String, Object>> getEngagementMetrics(
            @PathVariable @Parameter(description = "Bot ID") UUID botId) {
        try {
            BotStatistics stats = statisticsService.getBotStatistics(botId);
            Map<String, Object> engagement = Map.of(
                    "userRetentionRate", stats.getUserRetentionRate(),
                    "averageMessagesPerConversation", stats.getAvgMessagesPerConversation(),
                    "totalConversations", stats.getTotalConversations(),
                    "activeConversations", stats.getActiveConversations(),
                    "lastActivityTime", stats.getLastActivityTime()
            );
            return ResponseEntity.ok(engagement);
        } catch (Exception e) {
            log.error("Error getting engagement metrics for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
