package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.exceptions.WrongDeleteTokenException;
import com.chalyi.urlshortener.api.rest.dto.DeleteShortUrlRequestDto;
import com.chalyi.urlshortener.api.rest.dto.DeleteShortUrlResponseDto;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeleteShortUrlController {

    private final ShortUrlDeleteService shortUrlDeleteService;

    @RequestMapping(path = "/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody DeleteShortUrlRequestDto deleteShortUrlRequestDto) {
        try {
            shortUrlDeleteService.delete(deleteShortUrlRequestDto.getShortUrl(), deleteShortUrlRequestDto.getDeleteToken());
        } catch (NoSuchUrlFound noSuchUrlFound) {
            return ResponseEntity.notFound().build();
        } catch (WrongDeleteTokenException wrongDeleteTokenException) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new DeleteShortUrlResponseDto(wrongDeleteTokenException.getMessage()));
        }
        return ResponseEntity.ok(new DeleteShortUrlResponseDto("Deleted"));
    }
}
