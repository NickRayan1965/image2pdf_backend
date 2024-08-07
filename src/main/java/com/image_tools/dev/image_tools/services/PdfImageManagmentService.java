package com.image_tools.dev.image_tools.services;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.image_tools.dev.image_tools.models.PdfImageOptions;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PdfImageManagmentService {

  private final PdfMakerTools pdfMakerTools;
  private final IImageCompresor imageCompressor;
  private final float A4WIDTH_DEF = 21;
  private final float A4HEIGTH_DEF = 29.7f;
  private final float SEPARATOR = 0.5f;

  public Mono<ByteArrayResource> getImagesOnPdf(PdfImageOptions pdfImageOptions) {
    // try {
    System.out.println(pdfImageOptions);
    boolean isVertical = pdfImageOptions.isVertical();
    float A4WIDTH = isVertical ? A4WIDTH_DEF : A4HEIGTH_DEF;
    float A4HEIGTH = isVertical ? A4HEIGTH_DEF : A4WIDTH_DEF;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Float maxWidth = pdfImageOptions.getMaxWidth();
    Float maxHeight = pdfImageOptions.getMaxHeight();

    float marginLeft = .5f;
    float marginRight = .5f;
    float marginTop = .5f;
    float marginBottom = .5f;

    Document doc = new Document(isVertical ? PageSize.A4 : PageSize.A4.rotate(), marginLeft, marginRight, marginTop,
        marginBottom);
    PdfWriter.getInstance(doc, outputStream);

    doc.open();
    return getCompressedImagesFromPaths(pdfImageOptions.getImagePaths()).collectList().flatMap(images -> {
      try {
        float x_in_cm = marginLeft;
        float y_in_cm = A4HEIGTH - marginTop - maxHeight;
        boolean isFirstImageInLine = true;
        for (Image img : images) {
          boolean wasRotated = setRotationOfImage(img, maxHeight, maxWidth);
          float separator = isFirstImageInLine ? 0 : SEPARATOR;
          float totalWidth = separator + maxWidth;
          if (totalWidth + x_in_cm > (A4WIDTH - marginRight)) {
            x_in_cm = marginLeft;
            y_in_cm = y_in_cm - SEPARATOR - maxHeight;
            isFirstImageInLine = true;
            separator = 0;
          }
          if (y_in_cm - marginBottom < 0) {
            doc.newPage();
            y_in_cm = A4HEIGTH - marginTop - maxHeight;
            isFirstImageInLine = true;
            separator = 0;
          }
          this.setImageOnDoc(doc, img, maxWidth, maxHeight,
              x_in_cm + separator,
              y_in_cm, pdfImageOptions.isForceSize(), wasRotated);
          x_in_cm += pdfImageOptions.getMaxWidth() + separator;
          isFirstImageInLine = false;
        }

        doc.close();
        ByteArrayResource bytesArrayRes = new ByteArrayResource(outputStream.toByteArray());
        return Mono.just(bytesArrayRes);
      } catch (Exception e) {
        return Mono.error(e);
      }
    });
  }

  private boolean setRotationOfImage(Image img, float maxHeight, float maxWidth) {
    float imageHeight = img.getHeight();
    float imageWidth = img.getWidth();
    boolean isImageWider = imageWidth > imageHeight;
    boolean isSettingWider = maxWidth > maxHeight;
    if (isImageWider != isSettingWider) {
      img.setRotationDegrees(-90);
      return true;
    }
    return false;
  }

  private Flux<Image> getCompressedImagesFromPaths(List<String> paths) {
    return imageCompressor.compressImagesFromPaths(paths).map(fileBytes -> {
      try {
        return Image.getInstance(fileBytes.getBytes());

      } catch (Exception e) {
        return null;
      }
    });
  }

  private void setImageOnDoc(Document doc, Image img, float maxWidth, float maxHeigth, float x, float y,
      boolean forceSize, boolean wasRotated) {
    float maxWidthInPoints = pdfMakerTools.cmToPoints(maxWidth);
    float maxHeightInCm = pdfMakerTools.cmToPoints(maxHeigth);

    if (forceSize) {
      img.scaleAbsolute(wasRotated ? maxHeightInCm : maxWidthInPoints, wasRotated ? maxWidthInPoints : maxHeightInCm);
    } else {
      img.scaleToFit(maxWidthInPoints, maxHeightInCm);
    }
    img.setAbsolutePosition(pdfMakerTools.cmToPoints(x), pdfMakerTools.cmToPoints(y));
    doc.add(img);
  }
}
