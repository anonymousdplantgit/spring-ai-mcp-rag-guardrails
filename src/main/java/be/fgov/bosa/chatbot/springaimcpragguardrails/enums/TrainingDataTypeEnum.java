package be.fgov.bosa.chatbot.springaimcpragguardrails.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum TrainingDataTypeEnum {
    WEBSITE, DOCUMENT, PDF, FAQ
}
