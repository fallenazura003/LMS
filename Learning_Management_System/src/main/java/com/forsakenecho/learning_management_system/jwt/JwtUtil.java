package com.forsakenecho.learning_management_system.jwt;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "your-secret-key-must-be-at-least-32-characters-long-0975266380-9902!";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private final long jwtExpiration = 86400000; // 1 day


    // sinh token
    public  String generateToken(UserDetails user) {
        return Jwts.builder()
                .setSubject(user.getUsername()) //username là chủ thể của token
                .claim("role",user.getAuthorities().iterator().next().getAuthority()) //lấy role của người dùng(1 role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    // giải mã token -> lấy username
    public String extractUsername(String token) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
    }


    // xác thực token
    public boolean isTokenValid(String token,  UserDetails user) {
        return  (extractUsername(token).equals(user.getUsername()));
    }


}
