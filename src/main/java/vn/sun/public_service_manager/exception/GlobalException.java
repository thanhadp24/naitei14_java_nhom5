package vn.sun.public_service_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vn.sun.public_service_manager.dto.ApiResponseDTO;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponseDTO<Object> apiResponseDTO = new ApiResponseDTO<>();
        apiResponseDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        apiResponseDTO.setMessage("Lỗi xác thực: Thông tin đăng nhập không hợp lệ.");
        apiResponseDTO.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponseDTO);
    }
}
