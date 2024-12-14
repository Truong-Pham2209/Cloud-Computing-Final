package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {
	private static final String ACCESS_KEY = "";
	private static final String SECRET_KEY = "";
	
	@Bean
	S3Client s3Client() {
		return S3Client.builder()
				.region(Region.AP_SOUTHEAST_2)
	            .credentialsProvider(StaticCredentialsProvider.create(
	                    AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
				.build();
	}
}
