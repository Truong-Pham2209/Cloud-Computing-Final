package com.example.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DocumentDTO;
import com.example.dto.FileDTO;
import com.example.dto.FileType;
import com.example.service.DocumentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/documents")
@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FileController {
	DocumentService service;

	@GetMapping("")
	public ResponseEntity<List<DocumentDTO>> findDocument(@RequestParam(name = "fileName") String fileName) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(service.getAllByName(fileName));
	}

	@GetMapping("/student/")
	public ResponseEntity<List<DocumentDTO>> getStudentDocuments() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(service.getAll(FileType.STUDENT));
	}
	
	@GetMapping("/public/")
	public ResponseEntity<List<DocumentDTO>> getPublicDocuments() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(service.getAll(FileType.PUBLIC));
	}

	@GetMapping("/teacher/")
	public ResponseEntity<List<DocumentDTO>> getTeacherDocuments() {
		return ResponseEntity.status(HttpStatus.OK)
				.body(service.getAll(FileType.TEACHER));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> getFile(@PathVariable("id") String id) throws IOException {
		var response = service.getFile(id, false);

	    return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(response.getMime()))
	            .body(response.getData());
	}

	@GetMapping("/download/{id}")
	public ResponseEntity<Object> downloadFile(@PathVariable("id") String id) throws IOException {
		var response = service.getFile(id, false);
		String fileName = id.substring(id.lastIndexOf("/") + 1);
		
		MediaType mediaType = MediaType.parseMediaType(response.getMime());
		log.info(mediaType.toString());
		
		return ResponseEntity.ok()
				.contentType(mediaType)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(response.getData());
	}
	
	@GetMapping("/public/{id}")
	public ResponseEntity<Object> getPublicFile(@PathVariable("id") String id) throws IOException {
		var response = service.getFile(id, true);

	    return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(response.getMime()))
	            .body(response.getData());
	}

	@GetMapping("/public/download/{id}")
	public ResponseEntity<Object> downloadPublicFile(@PathVariable("id") String id) throws IOException {
		var response = service.getFile(id, true);
		String fileName = id.substring(id.lastIndexOf("/") + 1);
		
		MediaType mediaType = MediaType.parseMediaType(response.getMime());
		System.out.println(mediaType);
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(response.getMime()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(response.getData());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFile(@PathVariable("id") String id) throws IOException {
		service.deleteFile(id);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@PostMapping("/")
	public ResponseEntity<Object> saveFile(@RequestParam("document") MultipartFile multipartFile,
			@RequestParam("fileName") String fileName, @RequestParam("fileType") FileType fileType) throws IOException {

		FileDTO fileDto = FileDTO.builder().multipartFile(multipartFile).fileName(fileName).fileType(fileType).build();

		var responseDto = service.saveFile(fileDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}
}
