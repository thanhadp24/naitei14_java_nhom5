package vn.sun.public_service_manager.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "service_requirements")
@Data
public class ServiceRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private boolean required;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.required = true;
    }
}
