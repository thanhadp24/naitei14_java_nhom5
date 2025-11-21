package vn.sun.public_service_manager.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.sun.public_service_manager.dto.ApiResponseDTO;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        this.delegate.commence(request, response, authException); // run default send header 401

        // custom body response
        response.setContentType("application/json; charset=UTF-8");
        ApiResponseDTO<Object> apiResponseDTO = new ApiResponseDTO<>();
        apiResponseDTO.setStatus(response.getStatus());
        apiResponseDTO.setMessage("Lỗi xác thực: Vui lòng đăng nhập để truy cập tài nguyên.");
        String error = Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage());
        apiResponseDTO.setError(error);

        objectMapper.writeValue(response.getWriter(), apiResponseDTO);
    }

}
