package be.fgov.bosa.chatbot.springaimcpragguardrails;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Organization;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.OrganizationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
public class InitData implements ApplicationRunner {

    private final ChatbotRepository chatbotRepository;
    private final OrganizationRepository organizationRepository;
    static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1); // upper bound is exclusive
    }
    public static String generateRandomString(int length) {
        final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz  ";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(organizationRepository.count() > 0) {
            return;
        }
        Organization organization = organizationRepository.save(Organization.builder().name("Bosa").build());
        chatbotRepository.saveAll(Arrays.asList(
               Chatbot.builder()
                       .name("Ministry of justice bot")
                       .description("This is a bot for the ministry of justice")
                       .organization(organization)
                       .status(ChatbotStatusEnum.DRAFT)
                       .temperature(0.8).confidenceThreshold(0.8)
                       .systemPromptTemplate("You are a helpful and knowledgeable AI assistant designed to support the the ministry of justice. Your primary role is to assist with questions asked by users. This includes providing accurate and professional guidance on topics in the provided context. Respond only if the question is within the provided context. otherwise, say that you cannot respond to any other question not related to the provided context. Your response should be in the language that the question was asked.")
                       .build(),
                Chatbot.builder()
                        .name("Ministry of health bot")
                        .description("This is a bot for the ministry of health")
                        .organization(organization)
                        .status(ChatbotStatusEnum.DRAFT)
                        .temperature(0.8).confidenceThreshold(0.8)
                        .systemPromptTemplate("You are a helpful and knowledgeable AI assistant designed to support the the ministry of health. Your primary role is to assist with questions asked by users. This includes providing accurate and professional guidance on topics in the provided context. You can respond to greetings and farewells, any other response should be within the provided context, otherwise, say that you cannot responde to any other question not related to the provided context in the language that the question was asked.")
                        .build()));
    }
}
