package com.example.AerionSports_BE.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    // Chuỗi bí mật dài tối thiểu 32 ký tự
    private final String JWT_SECRET = "AerionSportsSecretKeyBaoMatTuyetDoi2026";
    private final long JWT_EXPIRATION = 86400000L; // 1 ngày tính bằng mili-giây

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Tạo token từ thông tin tài khoản và vai trò
    public String generateToken(String username, String loaiTaiKhoan, String vaiTro, Integer idChu) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .subject(username)
                .claims(Map.of(
                        "loai_tai_khoan", loaiTaiKhoan,
                        "vai_tro", vaiTro,
                        "id_chu_tai_khoan", idChu
                ))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Lấy username từ token
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // Lấy tất cả Claims (loai_tai_khoan, vai_tro) từ token
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Validate Token xem hợp lệ hay không
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}