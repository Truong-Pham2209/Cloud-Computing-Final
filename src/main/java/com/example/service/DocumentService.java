package com.example.service;

import java.util.List;

import com.example.dto.DocumentDTO;
import com.example.dto.FileDTO;
import com.example.dto.FileType;

public interface DocumentService {
	void deleteFile(String fileId);

	byte[] getFile(String fileId, boolean isPublicFile);

	List<DocumentDTO> getAll(FileType fileType);

	List<DocumentDTO> getAllByName(String name);

	DocumentDTO saveFile(FileDTO fileDTO);
}
