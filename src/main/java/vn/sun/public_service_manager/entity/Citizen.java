package vn.sun.public_service_manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "citizens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "dob", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date dob;// Date of birth

    // Ánh xạ kiểu ENUM của MySQL sang Java Enum
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "national_id", nullable = false, unique = true, length = 12)
    private String nationalId; // Dùng làm Username/Login ID

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password; // Mật khẩu đã được mã hóa

    @Column(name = "created_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}