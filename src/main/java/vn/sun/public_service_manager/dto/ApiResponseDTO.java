package vn.sun.public_service_manager.dto;

import lombok.Data;

@Data
public class ApiResponseDTO<T> {
    private Object message;
    private T data;
    private int status;
    private String error;
}
