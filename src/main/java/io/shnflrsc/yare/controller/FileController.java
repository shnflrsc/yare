package io.shnflrsc.yare.controller;

import io.shnflrsc.yare.model.File;
import io.shnflrsc.yare.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<File> getFile(@PathVariable Long id){
        Optional<File> file = fileService.findById(id);

        return file.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> uploadFile(@RequestParam MultipartFile fileUpload) {
        try {
            String url = fileService.uploadFile(fileUpload);

            File fileRecord = new File();

            fileRecord.setFileName(fileUpload.getOriginalFilename());
            fileRecord.setFileUrl(url);
            fileRecord.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));

            fileService.createFileRecord(fileRecord);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
