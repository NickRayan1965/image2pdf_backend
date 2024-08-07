package com.image_tools.dev.image_tools.services;

import java.util.List;

import org.springframework.http.codec.multipart.FilePart;

import com.image_tools.dev.image_tools.models.FileBytes;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IImageCompresor {
  Mono<FileBytes> compressImage(FilePart filePart);
  Mono<FileBytes> compressImage(String path);
  Flux<FileBytes> compressImages(List<FilePart> filePars);
  Flux<FileBytes> compressImagesFromPaths(List<String> paths);
}
