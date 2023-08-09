package com.chalyi.urlshortener.api.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteShortUrlRequestDto {
    @NotNull
    private String shortUrl;
    @NotNull
    private String deleteToken;
}
