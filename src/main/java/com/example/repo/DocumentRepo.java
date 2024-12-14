package com.example.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.DocumentEntity;
import com.example.dto.FileType;


@Repository
public interface DocumentRepo extends JpaRepository<DocumentEntity, Integer> {
	List<DocumentEntity> findAllByFileNameLike(String fileName);
	
	List<DocumentEntity> findAllByFileType(FileType fileType);

	Optional<DocumentEntity> findOneByFileId(String fileId);
	
	void deleteByFileId(String fileId);
}
