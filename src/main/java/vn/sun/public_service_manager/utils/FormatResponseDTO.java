package vn.sun.public_service_manager.utils;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import vn.sun.public_service_manager.dto.ApiResponseDTO;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@ControllerAdvice
public class FormatResponseDTO implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
            Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        int status = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);

        if (body instanceof String) {
            return body;
        }

        ApiResponseDTO<Object> responseDTO = new ApiResponseDTO<>();
        responseDTO.setStatus(status);
        // case success
        if (status < 400) {
            responseDTO.setMessage(apiMessage != null ? apiMessage.value() : "Success");
            responseDTO.setData(body);
        } else { // case error
            return body;
        }

        return responseDTO;
    }

}
