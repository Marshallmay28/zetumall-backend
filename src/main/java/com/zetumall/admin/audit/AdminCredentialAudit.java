package com.zetumall.admin.audit;

import com.zetumall.shared.BaseEntity;
import com.zetumall.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_credential_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCredentialAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "super_admin_id", nullable = false)
    private User superAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_admin_id", nullable = false)
    private User targetAdmin;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditActionType actionType;

    @Column(name = "old_value_hash")
    private String oldValueHash;

    @Column(name = "new_value_hash")
    private String newValueHash;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    public enum AuditActionType {
        PASSWORD_RESET,
        EMAIL_CHANGE,
        EMAIL_REMOVAL,
        ACCOUNT_LOCK,
        ACCOUNT_UNLOCK
    }
}
