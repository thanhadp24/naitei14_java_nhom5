package vn.sun.public_service_manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

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
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Column(name = "dob", nullable = false)
    // @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;// Date of birth

    // Ánh xạ kiểu ENUM của MySQL sang Java Enum
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "national_id", nullable = false, unique = true, length = 12)
    @NotBlank(message = "Số CMND/CCCD không được để trống")
    @Size(min = 9, max = 12, message = "Số CMND/CCCD phải từ 9 đến 12 ký tự")
    private String nationalId; // Dùng làm Username/Login ID

    @Column(name = "address", nullable = false, length = 255)
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Column(name = "phone", nullable = false, length = 15)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 15, message = "Số điện thoại phải từ 10 đến 15 ký tự")
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    @NotBlank(message = "Email không được để trống")
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