package com.example.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Integer> {
	Optional<UserEntity> findByUsername(String username);
	
	boolean existsByUsername(String username);
}
