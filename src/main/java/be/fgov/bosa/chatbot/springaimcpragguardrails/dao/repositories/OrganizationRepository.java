package be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    @Query("SELECT r FROM Organization r " +
            "WHERE (:value IS NULL OR  lower(r.name) like lower(concat('%', cast(:value as string ),'%')) ) "
    )
    Page<Organization> autocomplete( String value, Pageable pageable);
    @Query("SELECT r FROM Organization r " +
            "WHERE (:value IS NULL OR  lower(r.name) like lower(concat('%', cast(:value as string ),'%')) ) "
    )
    Page<Organization> findItems(String value, Pageable pageable);

}


