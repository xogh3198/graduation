package com.project.graduation.service.s3;

import com.project.graduation.config.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aws.s3", name = "bucket")
public class S3PresignService {

    private final AwsS3Properties awsS3Properties;
    private final S3Presigner s3Presigner;

    public PresignedUpload createPutUpload(String externalPlantId, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        String extension = extensionForContentType(normalizedContentType);
        String s3Key = buildObjectKey(externalPlantId, extension);
        Duration signatureDuration = Duration.ofMinutes(awsS3Properties.getPresignExpirationMinutes());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsS3Properties.getBucket())
                .key(s3Key)
                .contentType(normalizedContentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(signatureDuration)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toExternalForm();
        Instant expiresAt = Instant.now().plus(signatureDuration);

        return new PresignedUpload(
                uploadUrl,
                awsS3Properties.getBucket(),
                s3Key,
                normalizedContentType,
                buildPublicObjectUrl(s3Key),
                expiresAt,
                signatureDuration.getSeconds()
        );
    }

    private String buildObjectKey(String externalPlantId, String extension) {
        String prefix = awsS3Properties.getKeyPrefix();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + externalPlantId + "/photos/"
                + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + extension;
    }

    private String buildPublicObjectUrl(String s3Key) {
        return "https://" + awsS3Properties.getBucket()
                + ".s3." + awsS3Properties.getRegion()
                + ".amazonaws.com/" + s3Key;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "image/jpeg";
        }
        return contentType.trim();
    }

    private String extensionForContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/heic", "image/heif" -> ".heic";
            default -> ".jpg";
        };
    }

    public record PresignedUpload(
            String uploadUrl,
            String bucket,
            String s3Key,
            String contentType,
            String imageUrl,
            Instant expiresAt,
            long expiresInSeconds
    ) {
    }
}
