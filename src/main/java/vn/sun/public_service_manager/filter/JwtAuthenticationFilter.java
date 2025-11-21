package vn.sun.public_service_manager.filter;

import vn.sun.public_service_manager.service.JwtService;
import vn.sun.public_service_manager.service.CombinedUserDetailsService; // Sử dụng lớp service chung
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Sử dụng CombinedUserDetailsService để tải thông tin người dùng (Citizen/Staff)
    @Autowired
    private CombinedUserDetailsService combinedUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy Header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String nationalId; // Hoặc username, tùy thuộc vào subject trong token

        // 2. Kiểm tra Token có tồn tại và đúng định dạng Bearer không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Chuyển tiếp request và kết thúc, không cần xác thực JWT
        }

        // 3. Trích xuất Token (Bỏ qua "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Trích xuất ID người dùng từ Token (nationalId trong trường hợp Citizen)
        try {
            nationalId = jwtService.extractNationalId(jwt);
        } catch (Exception e) {
            // Log lỗi và chuyển tiếp. Nếu không thể trích xuất ID, coi như token không hợp lệ
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Xác thực và Thiết lập Security Context

        // nationalId phải tồn tại VÀ người dùng chưa được xác thực
        if (nationalId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Tải UserDetails (sử dụng nationalId làm identifier)
            UserDetails userDetails = this.combinedUserDetailsService.loadUserByUsername(nationalId);

            // Kiểm tra Token hợp lệ (chưa hết hạn và chữ ký đúng)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Tạo đối tượng xác thực (Authentication Token)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Thêm chi tiết về request
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ĐẶT Authentication vào Security Context
                // Sau bước này, Spring Security coi người dùng đã được xác thực
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Chuyển tiếp Request đến Filter tiếp theo hoặc Controller
        filterChain.doFilter(request, response);
    }
}