package com.mutsa.springboot_auction.domain.auctionImage.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file) throws IOException {
        String fileName = createFileName(file.getOriginalFilename());
        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(contentType)
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );
        }

        return s3Client.utilities()
                .getUrl(builder -> builder.bucket(bucket).key(fileName))
                .toExternalForm();
    }

    public String createFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "-" + originalFileName;
    }
}
