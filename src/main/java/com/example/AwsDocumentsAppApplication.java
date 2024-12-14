package com.example;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.dto.RoleEnum;
import com.example.entity.UserEntity;
import com.example.repo.UserRepo;

@SpringBootApplication
public class AwsDocumentsAppApplication {
	@Autowired
	private UserRepo repo;

	@Autowired
	private PasswordEncoder encoder;

	public static void main(String[] args) {
		SpringApplication.run(AwsDocumentsAppApplication.class, args);
	}

//	@formatter:off
	@Bean
	CommandLineRunner commandLineRunner() {
		return args -> {
			long total = repo.count();
			if(total > 0)
				return;
			
			String password = encoder.encode("12345678");
			
			@SuppressWarnings("deprecation")
			var teacher = UserEntity.builder()
					.username("teacher")
					.password(password)
					.phoneNumber("0123456789")
					.role(RoleEnum.TEACHER)
					.userCode("GV001")
					.birthday(new Date(1999, 1, 1))
					.build();
			
			@SuppressWarnings("deprecation")
			var student = UserEntity.builder()
					.username("student")
					.password(password)
					.phoneNumber("0123456999")
					.role(RoleEnum.STUDENT)
					.userCode("SV001")
					.birthday(new Date(1999, 1, 1))
					.build();
			
			repo.saveAll(Arrays.asList(student, teacher));
		};
	}
//	@formatter:om
}
