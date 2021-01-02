package com.events.eventsmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenUtil {

    //7 days equals 604800L seconds
    @Value("${auth.expiration}")
    private long TOKEN_VALIDITY = 604800L;

    @Value("${auth.secret}")
    private String TOKEN_SECRET;

    public String generateToken(UserDetails userDetails) {
        //add claims
        //sign with secret
        //specify expiration
        //compact to 1 string

        Map<String, Object> claims = new HashMap<>();

        //sub and created are values in the jwt object
        claims.put("sub", userDetails.getUsername());
        claims.put("created", new Date());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS256, TOKEN_SECRET)
                .compact();

    }

    private Date generateExpirationDate() {
        //token will expire 7 days from now
        return new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000);
    }

    private Claims extractClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().
                    setSigningKey(TOKEN_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public String getUserNameFromToken(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception exception) {
            return null;
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        String userName = getUserNameFromToken(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date tokenExpiration = extractClaims(token).getExpiration();
        return tokenExpiration.before(new Date());
    }
}
