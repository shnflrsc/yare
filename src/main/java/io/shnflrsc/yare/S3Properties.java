package io.shnflrsc.yare;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties (
    String bucketName,
    String region,
    String endpoint,
    String accessKey,
    String secretKey
){}
