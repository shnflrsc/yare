package io.shnflrsc.yare.service;

import io.shnflrsc.yare.FileTooLargeException;
import io.shnflrsc.yare.S3Properties;
import io.shnflrsc.yare.model.File;
import io.shnflrsc.yare.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final S3Client s3Client;
    private final S3Properties properties;
    private final FileRepository fileRepository;

    public Optional<File> findById(Long id) throws IllegalArgumentException {
        return fileRepository.findById(id);
    }

    public String uploadFile(MultipartFile fileUpload) throws FileTooLargeException, IOException {

        if (fileUpload.getSize() > 100000000) {
            throw new FileTooLargeException(fileUpload.getOriginalFilename());
        }

        String key = fileUpload.getOriginalFilename();

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

        return properties.endpoint() + "/" + key;
    }

    public void createFileRecord(File file) throws IllegalArgumentException {
        fileRepository.save(file);
    }
}
