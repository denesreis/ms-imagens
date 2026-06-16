package com.scasistemas.msbluedot.cloudflare;

import com.scasistemas.msbluedot.enums.StatusImagemEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudflareUploadResult {

    private String imageId;
    private String url;
    private String filename;
    private StatusImagemEnum status;
    private String errorMessage;

    public static CloudflareUploadResult success(String imageId, String url, String filename) {
        return CloudflareUploadResult.builder()
                .imageId(imageId)
                .url(url)
                .filename(filename)
                .status(StatusImagemEnum.ATIVO)
                .build();
    }

    public static CloudflareUploadResult error(String filename, String errorMessage) {
        return CloudflareUploadResult.builder()
                .filename(filename)
                .status(StatusImagemEnum.ERRO)
                .errorMessage(errorMessage)
                .build();
    }

    public boolean isSuccess() {
        return StatusImagemEnum.ATIVO.equals(status);
    }
}
