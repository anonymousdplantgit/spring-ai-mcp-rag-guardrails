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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<Chatbot> bots = chatbotRepository.saveAll(IntStream.range(0, getRandomInt(2, 10))
                .mapToObj(i -> {
                          Chatbot cb =  new Chatbot();
                            cb.setName("Bot "+i);
                            cb.setDescription(generateRandomString(getRandomInt(15, 25)));
                            cb.setOrganization(organization);
                            cb.setStatus(ChatbotStatusEnum.DRAFT);
                            return cb;
                })
                .collect(Collectors.toList()
                ));
    }
}
