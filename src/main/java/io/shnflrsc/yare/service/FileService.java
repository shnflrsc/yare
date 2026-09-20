package io.shnflrsc.yare.service;

import io.shnflrsc.yare.FileTooLargeException;
import io.shnflrsc.yare.S3Properties;
import io.shnflrsc.yare.model.File;
import io.shnflrsc.yare.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;
    private final S3Properties properties;
    private final FileRepository fileRepository;

    public Optional<File> findById(Long id) throws IllegalArgumentException {
        return fileRepository.findById(id);
    }

    public String uploadFile(MultipartFile fileUpload) throws FileTooLargeException, IOException, IllegalArgumentException {

        if (fileUpload.getSize() > 100000000) {
            throw new FileTooLargeException(fileUpload.getOriginalFilename());
        }

        String key = UUID.randomUUID().toString();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(fileUpload.getContentType())
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        fileUpload.getInputStream(),
                        fileUpload.getSize()
                )
        );

        String fileName = fileUpload.getOriginalFilename();
        String url = properties.endpoint() + "/" + key;
        Instant expiresAt = Instant.now().plus(Duration.ofHours(24));

        File fileRecord = new File();

        fileRecord.setFileName(fileName);
        fileRecord.setObjectKey(key);
        fileRecord.setFileUrl(url);
        fileRecord.setExpiresAt(expiresAt);

        fileRepository.save(fileRecord);

        return url;
    }

    public ResponseInputStream<GetObjectResponse> downloadFile(Long id) {
        File file = fileRepository.findById(id).orElseThrow(() -> new NoSuchElementException("File not found"));
        
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(properties.bucketName())
            .key(file.getObjectKey())
            .build();
        
        return s3Client.getObject(request);
    }
}
