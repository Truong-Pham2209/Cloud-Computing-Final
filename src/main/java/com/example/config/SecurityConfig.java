package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.security.JwtAuthenFilter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
	JwtAuthenFilter jwtAuthenFilter;
	AuthenticationEntryPoint authenticationEntryPoint;
	AccessDeniedHandler accessDeniedHandler;

//	@formatter:off
	@SuppressWarnings("removal")
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers("/api/get-token/").permitAll();
				auth.requestMatchers("/api/get-token/", "/h2-console", "/h2-console/**").permitAll();
				auth.requestMatchers("/api/documents/public/**").permitAll();
				auth.requestMatchers("/error").permitAll();
				auth.requestMatchers("/api/documents/teacher/**").hasAuthority("TEACHER");
				auth.requestMatchers(HttpMethod.POST, "/api/documents/**").hasAuthority("TEACHER");
				auth.requestMatchers(HttpMethod.DELETE, "/api/documents/**").hasAuthority("TEACHER");
				auth.anyRequest().authenticated();
			})
			.csrf(csrf -> csrf.disable())
			.headers(headers -> headers.frameOptions().disable())
			.sessionManagement(session -> {
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			})
			.exceptionHandling(ex -> {
				ex.authenticationEntryPoint(authenticationEntryPoint);
				ex.accessDeniedHandler(accessDeniedHandler);
			})
			.addFilterBefore(jwtAuthenFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
//	@formatter:on

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(BCryptVersion.$2B, 10);
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
