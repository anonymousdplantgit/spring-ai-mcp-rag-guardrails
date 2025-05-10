package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ChatController {


}
