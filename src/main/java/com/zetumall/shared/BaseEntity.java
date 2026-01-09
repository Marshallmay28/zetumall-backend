package com.zetumall.shared;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;  // Using CUID from Prisma

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = generateCuid();
        }
    }

    /**
     * Generate a CUID-like identifier
     * For production, consider using a library like https://github.com/timboudreau/cuid
     */
    private String generateCuid() {
        return "c" + System.currentTimeMillis() + 
               Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
