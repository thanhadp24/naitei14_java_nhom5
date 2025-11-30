package vn.sun.public_service_manager.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import vn.sun.public_service_manager.utils.BeanUtil;
import vn.sun.public_service_manager.utils.SecurityUtil;

@Entity
@Table(name = "applications")
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_code", length = 50, nullable = false, unique = true)
    private String applicationCode;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_staff", nullable = true)
    private User assignedStaff;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "application")
    private List<ApplicationDocument> documents;

    @OneToMany(mappedBy = "application")
    private List<ApplicationStatus> statuses;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        applicationCode = UUID.randomUUID().toString();
        SecurityUtil securityUtil = BeanUtil.getBean(SecurityUtil.class);
        citizen = securityUtil.getCurrentUserLogin();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
