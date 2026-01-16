package com.zetumall.admin.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminAuditRepository extends JpaRepository<AdminCredentialAudit, String> {
    List<AdminCredentialAudit> findByTargetAdminIdOrderByCreatedAtDesc(String targetAdminId);

    List<AdminCredentialAudit> findBySuperAdminIdOrderByCreatedAtDesc(String superAdminId);
}
