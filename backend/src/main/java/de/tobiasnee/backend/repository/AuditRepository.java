package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEntity, Long> {
}