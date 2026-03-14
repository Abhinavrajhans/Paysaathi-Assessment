package com.example.PaySathi.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncLog extends BaseEntity{

    @Column(name = "entity_type", nullable = false)
    private String entityType;       // "CUSTOMER", "INVOICE", "PAYMENT"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus status;       // SUCCESS, FAILED, IN_PROGRESS

    @Column(name = "records_fetched")
    private Integer recordsFetched;

    @Column(name = "records_saved")
    private Integer recordsSaved;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}