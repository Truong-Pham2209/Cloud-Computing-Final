package com.example.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccessDenied implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		log.error("Access denied. URI: {}, Message: {}", request.getRequestURI(), accessDeniedException.getMessage());

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter()
				.write(String.format("{\"error\": \"FORBIDDEN\", \"message\": \"Access is denied\", \"path\": \"%s\"}",
						request.getRequestURI()));
	}
}