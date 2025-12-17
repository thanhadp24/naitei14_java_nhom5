package vn.sun.public_service_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi citizenApi() {
        return GroupedOpenApi.builder()
                .group("citizen-api")
                .pathsToMatch("/api/citizen/**", "/api/v1/citizen/**", "/api/v1/applications/**", "/api/v1/services/**", "/api/v1/notifications/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("Public Service Manager Team");
        contact.setEmail("support@publicservice.vn");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
                .title("Public Service Manager - Citizen API")
                .version("1.0.0")
                .description("API documentation cho Công dân - Hệ thống quản lý dịch vụ công\n\n" +
                        "**Các API bao gồm:**\n" +
                        "- **Citizen Authentication**: Đăng ký, đăng nhập\n" +
                        "- **Citizen Profile**: Quản lý thông tin cá nhân\n" +
                        "- **Applications**: Nộp và quản lý hồ sơ\n" +
                        "- **Services**: Xem thông tin dịch vụ công\n" +
                        "- **Notifications**: Quản lý thông báo\n\n" +
                        "**Sử dụng JWT Authentication:**\n" +
                        "1. Đăng nhập qua `/api/v1/citizen/auth/login`\n" +
                        "2. Copy token từ response\n" +
                        "3. Click nút **Authorize** và nhập: `Bearer <token>`")
                .contact(contact)
                .license(license);

        // JWT Security Scheme
        io.swagger.v3.oas.models.security.SecurityScheme securityScheme = 
                new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Nhập JWT token sau khi đăng nhập (tự động thêm 'Bearer ' prefix)");

        io.swagger.v3.oas.models.security.SecurityRequirement securityRequirement = 
                new io.swagger.v3.oas.models.security.SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
