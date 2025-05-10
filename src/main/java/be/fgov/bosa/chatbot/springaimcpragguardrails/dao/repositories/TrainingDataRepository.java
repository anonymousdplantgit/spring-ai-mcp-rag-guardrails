package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.TrainingData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TrainingDataRepository extends JpaRepository<TrainingData, UUID> {
    @Query("SELECT t FROM TrainingData t " +
            "WHERE (:value IS NULL OR lower(t.title) LIKE lower(concat('%', cast(:value as string), '%')))")
    Page<TrainingData> autocomplete(String value, Pageable pageable);

    @Query("SELECT t FROM TrainingData t " +
            "WHERE (:value IS NULL OR lower(t.title) LIKE lower(concat('%', cast(:value as string), '%')))")
    Page<TrainingData> findItems(String value, Pageable pageable);
}
