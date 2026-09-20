
package io.shnflrsc.yare.dto;

import java.time.Instant;

public record FileResponseDto(String fileName, String fileUrl, Instant expiresAt) {

}