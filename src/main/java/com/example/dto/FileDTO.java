package com.example.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileDTO {
	@Nonnull
	MultipartFile multipartFile;

	@Nonnull
	String fileName;

	@Nonnull
	FileType fileType;
}
