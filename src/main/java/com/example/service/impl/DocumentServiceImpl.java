package com.example.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DocumentDTO;
import com.example.dto.DocumentResponse;
import com.example.dto.FileDTO;
import com.example.dto.FileType;
import com.example.entity.DocumentEntity;
import com.example.entity.UserEntity;
import com.example.repo.DocumentRepo;
import com.example.repo.UserRepo;
import com.example.service.DocumentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DocumentServiceImpl implements DocumentService {
	UserRepo userRepo;
	DocumentRepo documentRepo;
	S3Client s3Client;

	private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
	private static final Set<String> FILE_EXTENSIONS = Set.of("txt", "text", "docx", "pdf", "ppt", "pptx", "zip", "rar", "mp4",
			"mkv", "avi", "jpg", "jpeg", "png");
	private static final Map<String, String> MIME_TYPES = new HashMap<>();
	static {
	    MIME_TYPES.put("txt", "text/plain");
	    MIME_TYPES.put("text", "text/plain");
	    MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	    MIME_TYPES.put("pdf", "application/pdf");
	    MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
	    MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
	    MIME_TYPES.put("zip", "application/zip");
	    MIME_TYPES.put("rar", "application/vnd.rar");
	    MIME_TYPES.put("mp4", "video/mp4");
	    MIME_TYPES.put("mkv", "video/x-matroska");
	    MIME_TYPES.put("avi", "video/x-msvideo");
	    MIME_TYPES.put("jpg", "image/jpeg");
	    MIME_TYPES.put("jpeg", "image/jpeg");
	    MIME_TYPES.put("png", "image/png");
	}
	
	private static final String BUCKET_NAME = "spring-boot--documents-app";

//	@formatter:off
	@Override
	@Transactional
	public void deleteFile(String fileId) {
		var document = documentRepo.findOneByFileId(fileId)
				.orElseThrow(() -> new RuntimeException("Does not exists fileId: " + fileId));
		
		String folder = getFolderByFileType(document.getFileType());
		String fileKey = folder +  fileId
				+ "-" + document.getFileName().trim();
		log.info(fileKey);
		
	    if (!isFileExists(fileKey)) {
	        throw new IllegalArgumentException("File not found");
	    }

	    deleteFileFromS3(fileKey);
	    documentRepo.deleteByFileId(fileId);
	}

	@Override
	public DocumentResponse getFile(String fileId, boolean isPublicFile) {
		try {			
			if(!isPublicFile) {
				if(!isLoggedIn())
					throw new RuntimeException("Unauthorized");
			}
			
			var document = documentRepo.findOneByFileId(fileId)
					.orElseThrow(() -> new RuntimeException("Does not exists fileId: " + fileId));
			
			if(isPublicFile && !document.getFileType().equals(FileType.PUBLIC))
				throw new RuntimeException("This file is not public file");
					
			if(document.getFileType().equals(FileType.TEACHER)) {
				if(!containsAnyRole(List.of("TEACHER")))
					throw new RuntimeException("Access Denied");
			}
			
			String folder = getFolderByFileType(document.getFileType());
			String fileKey = folder +  fileId
					+ "-" + document.getFileName().trim();
			
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(BUCKET_NAME)
					.key(fileKey)
					.build();
			String mime = MIME_TYPES.get(document.getFileExtension());

			var s3Object = s3Client.getObject(getObjectRequest);
			return new DocumentResponse(mime, s3Object.readAllBytes());
		} catch (SdkException | IOException e) {
			log.error("Failed to fetch file with ID: {} from S3", fileId, e);
			throw new RuntimeException("Failed to get file from S3", e);
		}
	}
//	@formatter:on

	@Override
	public List<DocumentDTO> getAll(FileType fileType) {
		var documents = documentRepo.findAllByFileType(fileType);
		return documents.stream().map(d -> toDto(d)).toList();
	}

	@Override
	public List<DocumentDTO> getAllByName(String name) {
		var documents = documentRepo.findAllByFileNameLike(name);
		return documents.stream().map(d -> toDto(d)).toList();
	}

	@Override
	@Transactional
	public DocumentDTO saveFile(FileDTO fileDTO) {
		validateFile(fileDTO.getMultipartFile());
		String fileId = uploadFileToS3(fileDTO);
		String extension = getFileExtension(fileDTO.getMultipartFile().getOriginalFilename());

		var document = toEntity(fileDTO);
		document.setFileId(fileId);
		document.setFileExtension(extension);
		documentRepo.save(document);

		return toDto(document);
	}

//	@formatter:off	
	private void deleteFileFromS3(String fileId) {
	    try {
	        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
	                .bucket(BUCKET_NAME)
	                .key(fileId)
	                .build();
	
	        s3Client.deleteObject(deleteObjectRequest);
	        
	    } catch (SdkException e) {
	        if (e instanceof AwsServiceException awsServiceException) {
	            log.error("S3 SDK error: {} - {}", awsServiceException.awsErrorDetails().errorCode(),
	                    awsServiceException.awsErrorDetails().errorMessage(), e);
	        } else {
	            log.error("Unexpected SDK exception: {}", e.getMessage(), e);
	        }
	        throw new RuntimeException("S3 SDK error", e);
	    } catch (Exception e) {
	        log.error("Unexpected error while deleting file from S3: {}", e.getMessage(), e);
	        throw new RuntimeException("Unexpected error while deleting file from S3", e);
	    }
	}

	private boolean isFileExists(String fileId) {
	    try {
	        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
	                .bucket(BUCKET_NAME)
	                .key(fileId)
	                .build();
	        s3Client.headObject(headObjectRequest); 
	        return true;
	    } catch (NoSuchKeyException e) {
	        log.warn("File not found in S3 with fileId: {}", fileId);
	        return false;
	    } catch (SdkException e) {
	        if (e instanceof AwsServiceException awsServiceException) {
	            log.error("S3 SDK error: {} - {}", awsServiceException.awsErrorDetails().errorCode(),
	                    awsServiceException.awsErrorDetails().errorMessage(), e);
	        } else {
	            log.error("Unexpected SDK exception: {}", e.getMessage(), e);
	        }
	        throw new RuntimeException("S3 SDK error", e);
	    } catch (Exception e) {
	        log.error("Unexpected error while checking file existence on S3: {}", e.getMessage(), e);
	        throw new RuntimeException("Unexpected error while checking file existence on S3", e);
	    }
	}
	
	private String getFolderByFileType(FileType fileType) {
		switch (fileType) {
		case TEACHER:
			return "teacher/";
		case STUDENT:
			return "student/";
		case PUBLIC:
			return "public/";
		default:
			throw new IllegalArgumentException("Unknown file type: " + fileType);
		}
	}

	private UserEntity getCurrentUser() {
		var principal = SecurityContextHolder.getContext().getAuthentication();
		if (principal == null)
			throw new RuntimeException("Unauthorized");
		String username = ((UserDetails) principal.getPrincipal()).getUsername();
		var user = userRepo.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Username does not exists!!!"));
		return user;
	}
	
	private boolean isLoggedIn() {
		var principal = SecurityContextHolder.getContext().getAuthentication();
		return principal != null;
	}
	
	private boolean containsAnyRole(List<String> roles) {
		var principal = SecurityContextHolder.getContext().getAuthentication();
		if (principal == null)
			throw new RuntimeException("Unauthorized");
		return principal.getAuthorities().stream().anyMatch(a -> roles.contains(a.getAuthority()));
	}
	
	private String uploadFileToS3(FileDTO dto) {
		try {
			String folder = getFolderByFileType(dto.getFileType());
			String fileId = UUID.randomUUID().toString();
			String fileKey = folder +  fileId
					+ "-" + dto.getFileName().trim();

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(BUCKET_NAME)
					.key(fileKey)
					.contentType(dto.getMultipartFile().getContentType())
					.build();
			
			InputStream fileInputStream = dto.getMultipartFile().getInputStream();
	        if (fileInputStream == null) {
	            log.error("Failed to get InputStream for file: {}", dto.getFileName());
	            throw new RuntimeException("InputStream for file is null");
	        }

	        s3Client.putObject(putObjectRequest, 
	        		RequestBody.fromInputStream(fileInputStream, dto.getMultipartFile().getSize()));
	        log.info("File uploaded successfully to S3 with key: {}", fileKey);
			return fileId;
	    } catch (IOException e) {
	        log.error("Failed to upload file to S3 due to IO exception: {}", e.getMessage(), e);
	        throw new RuntimeException("Failed to upload file to S3", e);
	    } catch (SdkException e) {
	        if (e instanceof AwsServiceException awsServiceException) {
	            log.error("S3 SDK error: {} - {}", awsServiceException.awsErrorDetails().errorCode(),
	                    awsServiceException.awsErrorDetails().errorMessage(), e);
	        } else {
	            log.error("Unexpected SDK exception: {}", e.getMessage(), e);
	        }
	        throw new RuntimeException("S3 SDK error", e);
	    } catch (Exception e) {
	        log.error("Unexpected error while uploading file to S3: {}", e.getMessage(), e);
	        throw new RuntimeException("Unexpected error while uploading file to S3", e);
	    }
	}
	
	private void validateFile(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("File name is invalid");
		}

		String extension = getFileExtension(fileName);
        if (!FILE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 50MB");
        }
	}

	private String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
			throw new IllegalArgumentException("File does not have a valid extension");
		}
		return fileName.substring(dotIndex + 1);
	}
	
	private DocumentDTO toDto(DocumentEntity entity) {
		return DocumentDTO.builder()
				.fileId(entity.getFileId())
				.fileName(entity.getFileName())
				.uploadAt(entity.getUploadAt())
				.build();
	}
	
	private DocumentEntity toEntity(FileDTO dto) {
		var user = getCurrentUser();
		var document = DocumentEntity.builder()
				.fileName(dto.getFileName())
				.uploadAt(new Date())
				.fileType(dto.getFileType())
				.user(user)
				.build();
		return document;
	}
//	@formatter:on
}
