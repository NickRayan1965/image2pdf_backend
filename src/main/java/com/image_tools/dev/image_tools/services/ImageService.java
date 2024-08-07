package com.image_tools.dev.image_tools.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import com.image_tools.dev.image_tools.models.FileBytes;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class ImageService implements IImageService {

  private final IImageCompresor imageCompressor;

  @Override
  public Mono<ByteArrayResource> getCompressedImage(FilePart filePart) {
    return imageCompressor.compressImage(filePart).map(fileImageBytes -> {
      return new ByteArrayResource(fileImageBytes.getBytes());
    });
  }

  @Override
  public Mono<ByteArrayResource> getCompressedImagesAsZip(List<FilePart> fileParts) {
    return imageCompressor.compressImages(fileParts)
        .collectList()
        .flatMap(this::createZipFile)
        .map(zipBytes -> {
          return new ByteArrayResource(zipBytes);
        });
  }

  private Mono<byte[]> createZipFile(List<FileBytes> imageFileBytes) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(baos);
      for (int i = 0; i < imageFileBytes.size(); i++) {
        FileBytes imageBytes = imageFileBytes.get(i);
        ZipEntry entry = new ZipEntry("image_" + (i + 1) + "." + imageBytes.getExt());
        zos.putNextEntry(entry);
        zos.write(imageBytes.getBytes());
        zos.closeEntry();
      }
      zos.finish();
      return Mono.just(baos.toByteArray());
    } catch (Exception e) {
      return Mono.error(e);
    }
  }

}
