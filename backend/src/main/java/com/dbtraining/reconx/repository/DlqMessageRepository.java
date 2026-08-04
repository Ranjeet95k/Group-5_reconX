package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.model.DlqMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================================
 * TICKET-ADV136 — DlqMessage repository (supporting model for DlqConsumer /
 *                 DlqAdminController)
 *
 * WHAT:    Spring Data JPA repository over dlq_messages.
 * HOW:     findByEventId(UUID) backs the one-at-a-time replay lookup in
 *          DlqAdminController; findAll()/delete() come from JpaRepository.
 * WHY:     Replay must target a single event by its idempotency key, never
 *          bulk-replay the whole DLQ (see ADV136 Hint 1).
 * ============================================================================
 */
public interface DlqMessageRepository extends JpaRepository<DlqMessage, Long> {

    Optional<DlqMessage> findByEventId(UUID eventId);
}