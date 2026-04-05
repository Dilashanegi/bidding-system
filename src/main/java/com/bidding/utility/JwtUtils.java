package com.bidding.utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

// JWT = JSON Web Token
// Yeh ek token hota hai jo login ke baad milta hai
// Har request mein yeh token bhejte hain taaki server jaane ki tum logged-in ho
@Component
public class JwtUtils {

    // Secret key - token sign karne ke liye (kisi ko bhi share mat karo)
    private static final String SECRET = "biddingSystemSecretKey2024ThisMustBeLong!";
    private static final long EXPIRY_MS = 24 * 60 * 60 * 1000; // 24 hours

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Token banao - login ke baad call hota hai
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                .signWith(getKey())
                .compact();
    }

    // Token se email nikalo
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Token valid hai ya nahi check karo
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
