package com.example.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.constant.SecurityConstant;
import com.example.service.JwtGenerateService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JwtGenerateServiceImpl implements JwtGenerateService {

	@Override
	public String generateToken(String userName) {
		String accessToken = createAccessToken(userName);
		if (!StringUtils.hasText(accessToken))
			return null;

		return accessToken;
	}

	@Override
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public Boolean validateToken(String token, String username) {
		String usernameFromToken = extractUsername(token);
		Date dateExprire = extractClaim(token, Claims::getExpiration);

		return (username.equals(usernameFromToken) && dateExprire.after(new Date()));
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		try {
			Claims claims = Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
			return claimsResolver.apply(claims);
		} catch (SignatureException e) {
			log.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}
		return null;
	}

	private String createAccessToken(String userName) {
		return Jwts.builder().claims(generateClaims(userName)).subject(userName)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + SecurityConstant.JWT_EXPIRED))
				.signWith(getSecretKey(), Jwts.SIG.HS256).compact();
	}

	private Map<String, Object> generateClaims(String username) {
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put("author", "Phạm Trường");
		claims.put("github_repo", "https://github.com/TruongPham2209/Demo-Spring-Boot-Features/tree/master/demo-jwt");
		claims.put("username", username);
		claims.put("token_type", "Bearer");

		return claims;
	}

	private SecretKey getSecretKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SecurityConstant.JWT_SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
