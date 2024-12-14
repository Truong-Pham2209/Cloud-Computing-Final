package com.example.service;

public interface JwtGenerateService {
	String generateToken(String userName);

	String extractUsername(String token);

	Boolean validateToken(String token, String username);
}
