package vn.sun.public_service_manager.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolationException;
import vn.sun.public_service_manager.dto.ApiResponseDTO;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);
        response.setMessage(ex.getMessage());
        response.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);
        response.setMessage("Invalid username or password");
        response.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(401).body(response);
    }

    @ExceptionHandler(value = { FileException.class })
    public ResponseEntity<ApiResponseDTO<Object>> handleFileException(Exception ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);
        response.setMessage(ex.getMessage());
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMaxFileSizeException(MaxUploadSizeExceededException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);
        response.setMessage("File size exceeds the maximum allowed limit. Please upload a smaller file.");
        response.setError("MaxUploadSizeExceededException");
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);

        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getDefaultMessage())
                .toList();

        response.setMessage("Validation failed: " + messages);
        response.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);

        List<String> messages = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getMessage())
                .toList();

        response.setMessage("Validation failed: " + messages);
        response.setError(ConstraintViolationException.class.getSimpleName());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGeneralException(Exception ex) {
        ApiResponseDTO<Object> response = new ApiResponseDTO<>();
        response.setData(null);
        response.setMessage("An unexpected error occurred");
        response.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(500).body(response);
    }
}
