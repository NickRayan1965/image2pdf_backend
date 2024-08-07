package com.image_tools.dev.image_tools.services;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface IImageService {
  Mono<ByteArrayResource> getCompressedImage(FilePart filePart);
  Mono<ByteArrayResource> getCompressedImagesAsZip(List<FilePart> fileParts);
}
