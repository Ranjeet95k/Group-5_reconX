package com.dbtraining.reconx.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dbtraining.reconx.repository.entity.DlqMessage;

@Repository
public interface DlqMessageRepository extends JpaRepository<DlqMessage, UUID> {
    Optional<DlqMessage> findByEventId(UUID eventId);
}