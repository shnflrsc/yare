
package io.shnflrsc.yare;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service 
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(
            clientId,
            key -> createBucket()
        );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket() {
    Bandwidth limit = Bandwidth.builder()
            .capacity(5)
            .refillGreedy(5, Duration.ofMinutes(1))
            .build();

    return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}