package com.image_tools.dev.image_tools.controllers;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.image_tools.dev.image_tools.models.PdfImageOptions;
import com.image_tools.dev.image_tools.services.PdfImageManagmentService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("image-pdf")
@AllArgsConstructor
public class ImagePdfController {
  private final PdfImageManagmentService pdfImageManagmentService;

  @PostMapping("convert-to-pdf")
  public Mono<ResponseEntity<ByteArrayResource>> convertToPdf(
    @RequestBody() PdfImageOptions options
  ) {
    return pdfImageManagmentService.getImagesOnPdf(options).map(pdfBytes -> {
      return ResponseEntity
          .ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
          .contentType(MediaType.APPLICATION_PDF)
          .contentLength(pdfBytes.contentLength())
          .body(pdfBytes);
    });
  }
}
