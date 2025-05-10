package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotStatusRequest {
    @NotNull
    private ChatbotStatusEnum status;
}
