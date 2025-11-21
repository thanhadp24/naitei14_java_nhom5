package vn.sun.public_service_manager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "processing_time")
    private Integer processingTime;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal fee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_department", referencedColumnName = "id")
    private Department responsibleDepartment;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }
    }
}
