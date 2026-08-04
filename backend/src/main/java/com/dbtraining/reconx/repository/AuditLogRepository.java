package com.dbtraining.reconx.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dbtraining.reconx.repository.entity.AuditLogEntry;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    @Query("SELECT a FROM AuditLogEntry a WHERE a.tradeRef = :tradeRef ORDER BY a.eventTimestamp ASC")
    List<AuditLogEntry> findByTradeRefOrderByOccurredAtAsc(@Param("tradeRef") String tradeRef);

    // Derived Spring Data query method matching the exact field name
    List<AuditLogEntry> findByTradeRefOrderByEventTimestampAsc(String tradeRef);
}