package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    List<Resource> findByTrainingDataId(UUID trainingDataId);
}
