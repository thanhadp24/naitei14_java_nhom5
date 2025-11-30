package vn.sun.public_service_manager.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import vn.sun.public_service_manager.utils.SecurityUtil;
import vn.sun.public_service_manager.utils.constant.UploadType;

@Entity
@Table(name = "application_documents")
@Data
public class ApplicationDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadBy;
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UploadType type;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
        this.uploadBy = SecurityUtil.getCurrentUserName();
    }
}
