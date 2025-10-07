package com.marketnest.ecommerce.service.auth;

import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms:900000}")
    private long jwtExpirationMs;

    public String generateToken(Authentication authentication) {
        return generateToken(authentication, null);
    }

    public String generateToken(Authentication authentication, String tokenId) {
        SecretKey key = getSigningKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", authentication.getName());
        claims.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        if (tokenId != null) {
            claims.put("tid", tokenId);
        }

        return Jwts.builder()
                .claims(claims)
                .issuer("Market Nest")
                .subject(authentication.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public String generateToken(String email, String authorities) {
        SecretKey key = getSigningKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("authorities", authorities);

        return Jwts.builder()
                .claims(claims)
                .issuer("Market Nest")
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public Authentication validateToken(String token) {
        SecretKey key = getSigningKey();

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String authorities = claims.get("authorities", String.class);
        String email = claims.get("email", String.class);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty() || !userOptional.get().isActive()) {
            throw new BadCredentialsException("User is disabled or not found");
        }

        User user = userOptional.get();
        Date tokenIssuedAt = claims.getIssuedAt();

        if (user.getPasswordChangedAt() != null &&
            tokenIssuedAt.before(Date.from(user.getPasswordChangedAt()))) {
            throw new BadCredentialsException("Password has been changed. Please login again");
        }

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)
        );
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

}
