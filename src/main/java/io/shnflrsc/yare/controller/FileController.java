package io.shnflrsc.yare.controller;

import io.shnflrsc.yare.FileTooLargeException;
import io.shnflrsc.yare.RateLimitService;
import io.shnflrsc.yare.model.File;
import io.shnflrsc.yare.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import io.shnflrsc.yare.dto.FileResponseDto;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final RateLimitService rateLimitService;

    public FileController(FileService fileService, RateLimitService rateLimitService) {
        this.fileService = fileService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<File> getFile(HttpServletRequest request, @PathVariable Long id){
        
        String clientId = request.getRemoteAddr();

        if (!rateLimitService.allowRequest(clientId)) {
            return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        }
        
        Optional<File> file = fileService.findById(id);

        return file.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FileResponseDto> uploadFile(HttpServletRequest request, @RequestParam MultipartFile fileUpload) {
        
        String clientId = request.getRemoteAddr();

        if (!rateLimitService.allowRequest(clientId)) {
            return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .build();
        }

        try {
            String fileName = fileUpload.getOriginalFilename();
            String url = fileService.uploadFile(fileUpload);
            Instant expiresAt = Instant.now().plus(Duration.ofHours(24));
            
            FileResponseDto dto = new FileResponseDto(
                fileName,
                url,
                expiresAt
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.internalServerError().build();
        } catch (FileTooLargeException e) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
        @PathVariable Long id
    ) {
        ResponseInputStream<GetObjectResponse> inputStream =
            fileService.downloadFile(id);

        GetObjectResponse response = inputStream.response();

        Optional<File> file = fileService.findById(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(response.contentType()))
            .contentLength(response.contentLength())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.get().getFileName() + "\""
            )
            .body(new InputStreamResource(inputStream));
    }
} 
