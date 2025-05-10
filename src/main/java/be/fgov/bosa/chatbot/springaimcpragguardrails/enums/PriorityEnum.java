package be.fgov.bosa.chatbot.springaimcpragguardrails.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum PriorityEnum {
    TOP_PRIORITY,
    VERY_HIGH,
    HIGH,
    DEFAULT,
    LOW

}
