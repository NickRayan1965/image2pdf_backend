package com.image_tools.dev.image_tools.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.image_tools.dev.image_tools.services.IImageService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping("images")
@AllArgsConstructor
public class ImageController {
  private final IImageService imageService;

  @PostMapping("/compress")
  public Mono<ResponseEntity<ByteArrayResource>> compressImage(
      @RequestPart("file") Mono<FilePart> imageFilePart) {
    return imageFilePart
        .flatMap(image -> {
          return imageService.getCompressedImage(image);
        })
        .map(byteArrayResImage -> {
          return ResponseEntity
              .ok()
              .contentLength(byteArrayResImage.contentLength())
              .body(byteArrayResImage);
        });
  }

  @PostMapping("/compress-many-and-return-as-zip")
  public Mono<ResponseEntity<ByteArrayResource>> compressImagesAndReturnAsZip(
      @RequestPart("files") Flux<FilePart> imageFileParts) {
    return imageFileParts.collectList().flatMap(images -> {
      return imageService.getCompressedImagesAsZip(images);
    })
        .map(zipBytes -> {
          return ResponseEntity
              .ok()
              .contentLength(zipBytes.contentLength())
              .body(zipBytes);
        });

  }

}
