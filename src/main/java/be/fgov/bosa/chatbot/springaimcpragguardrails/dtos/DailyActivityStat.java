package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

import java.time.LocalDate; /**
 * Projection interfaces for statistics queries
 */
public interface DailyActivityStat {
    LocalDate getDate();
    Long getCount();
}
