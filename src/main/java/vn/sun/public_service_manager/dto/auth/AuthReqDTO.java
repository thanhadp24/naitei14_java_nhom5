package vn.sun.public_service_manager.dto.auth;

import lombok.Data;

@Data
public class AuthReqDTO {
    private String email;
    private String password;
}
