package com.example.entity;

import java.util.Date;

import com.example.dto.RoleEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer id;

	@Column(nullable = false, unique = true)
	String userCode;

	@Column(nullable = false, unique = true)
	String username;

	@Column(nullable = false)
	String password;

	@Column(nullable = false, unique = true)
	String phoneNumber;

	@Column(nullable = false)
	Date birthday;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	RoleEnum role;
}
