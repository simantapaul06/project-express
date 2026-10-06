package com.projectexpress.api.profile_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket}")
    private String bucketName;

    public void upload(String objectKey, MultipartFile file) {

        try {

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public byte[] download(String objectKey) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(request);

        return object.asByteArray();
    }

    public void delete(String objectKey) {

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(request);
    }
}