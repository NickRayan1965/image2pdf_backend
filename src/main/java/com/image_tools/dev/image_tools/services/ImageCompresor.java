package com.image_tools.dev.image_tools.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.image_tools.dev.image_tools.models.FileBytes;
import com.image_tools.dev.image_tools.models.ImagePathBody;

import net.coobird.thumbnailator.Thumbnails;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ImageCompresor implements IImageCompresor {
  @Override
  public Mono<FileBytes> compressImage(FilePart filePart) {
    return DataBufferUtils.join(filePart.content())
        .flatMap(dataBuffer -> {
          try {
            byte[] imageBytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(imageBytes);
            int sizeInKB = imageBytes.length / 1000;
            String filename = filePart.filename();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (sizeInKB < 1000) {
              FileBytes fileBytes = FileBytes
                  .builder()
                  .bytes(imageBytes)
                  .originalName(filename)
                  .ext(this.getExtensionFromOriginalName(filename))
                  .build();
              return Mono.just(fileBytes);
            }
            Thumbnails.of(inputStream).size(1200, 800)
                .outputQuality(1)
                .toOutputStream(outputStream);
            DataBufferUtils.release(dataBuffer);
            FileBytes fileBytes = FileBytes
                .builder()
                .bytes(outputStream.toByteArray())
                .originalName(filename)
                .ext(this.getExtensionFromOriginalName(filename))
                .build();
            return Mono.just(fileBytes);
          } catch (Exception e) {
            return Mono.error(e);
          }
        });
  }

  @Override
  public Flux<FileBytes> compressImages(List<FilePart> fileParts) {
    return Flux.fromIterable(fileParts)
        .flatMap(this::compressImage);

  }

  @Override
  public Flux<FileBytes> compressImagesFromPaths(List<String> paths) {
    return Flux.fromIterable(paths)
        .flatMap(this::compressImage);

  }

  private String getExtensionFromOriginalName(String originalFilename) {
    String extension = "";
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex != -1) {
      extension = originalFilename.substring(dotIndex + 1);
    }
    return extension;
  }

  @Override
  public Mono<FileBytes> compressImage(String path) {
    Path pathFile = Paths.get(path);
    return Mono.just(
        pathFile
        )
        .map(pathFileToRead -> {
          try {
            return Files.readAllBytes(pathFileToRead);
          } catch (IOException e) {
            return null;
          }
        })
        .flatMap(bytes -> {
          int sizeInKB = bytes.length / 1000;
          ByteArrayInputStream inputArrayStream = new ByteArrayInputStream(bytes);
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          String filename = getFileNameFromPath(path);
          if (sizeInKB < 1000) {
            FileBytes fileBytes = FileBytes
                .builder()
                .bytes(bytes)
                .originalName(filename)
                .ext(this.getExtensionFromOriginalName(filename))
                .build();
            return Mono.just(fileBytes);
          }
          try {
            Thumbnails.of(inputArrayStream).size(1200, 800)
                .outputQuality(1)
                .toOutputStream(outputStream);
            FileBytes fileBytes = FileBytes
                .builder()
                .bytes(outputStream.toByteArray())
                .originalName(filename)
                .ext(this.getExtensionFromOriginalName(filename))
                .build();
            return Mono.just(fileBytes);
          } catch (Exception e) {
            return Mono.error(e);
          }
        });
  }

  private String getFileNameFromPath(String path) {
    String normalizedPath = path.replace("\\", "/");
    int lastSeparatorIndex = normalizedPath.lastIndexOf("/");

    if (lastSeparatorIndex == -1) {
      return path;
    }
    return normalizedPath.substring(lastSeparatorIndex + 1);
  }

  @Override
  public Flux<List<FileBytes>> compressImagesFromImgPathBodies(List<ImagePathBody> imgPathBodies) {
    return Flux.fromIterable(imgPathBodies)
    .flatMap(imgPathBody -> {
      return Flux.range(0, imgPathBody.getNumberOfCopies())
      .flatMap( i -> this.compressImage(imgPathBody.getPath()))
      .collectList()
      ;
    });
  }
}
