package vn.sun.public_service_manager.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Khóa bí mật được đọc từ application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Thời gian hết hạn được đọc từ application.properties (milliseconds)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // =========================================================================
    // 1. TẠO TOKEN (GENERATION)
    // =========================================================================

    /**
     * Tạo JWT Token cho người dùng (Citizen).
     * @param nationalId - National ID của công dân (dùng làm subject)
     * @return Chuỗi JWT Token
     */
    public String generateToken(String nationalId) {
        // Thêm các thông tin bổ sung (claims) vào token nếu cần
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "CITIZEN"); // Gắn vai trò vào token
        return buildToken(claims, nationalId, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject) // Đặt nationalId làm subject (định danh người dùng)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Ký token bằng khóa bí mật
                .compact();
    }

    // =========================================================================
    // 2. GIẢI MÃ & TRÍCH XUẤT (EXTRACTION)
    // =========================================================================

    /**
     * Trích xuất nationalId (Subject) từ JWT Token.
     */
    public String extractNationalId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất ngày hết hạn (Expiration Date) từ token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Phương thức chung để trích xuất một claim cụ thể từ token.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Giải mã và trích xuất tất cả Claims (Payload) từ token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =========================================================================
    // 3. XÁC THỰC (VALIDATION)
    // =========================================================================

    /**
     * Kiểm tra xem JWT Token có hợp lệ không.
     * Kiểm tra 1: Subject (nationalId) khớp với UserDetails.
     * Kiểm tra 2: Token chưa hết hạn.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String nationalId = extractNationalId(token);
        // Kiểm tra nationalId trong token có khớp với username (nationalId) của UserDetails không
        return (nationalId.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        // Kiểm tra xem ngày hết hạn có trước thời điểm hiện tại không
        return extractExpiration(token).before(new Date());
    }

    // =========================================================================
    // 4. KHÓA BÍ MẬT (SECRET KEY)
    // =========================================================================

    /**
     * Chuyển đổi khóa bí mật (Base64) thành đối tượng Key.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}