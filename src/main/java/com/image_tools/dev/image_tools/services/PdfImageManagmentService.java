package com.image_tools.dev.image_tools.services;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.image_tools.dev.image_tools.models.ImagePagination;
import com.image_tools.dev.image_tools.models.MarginConfig;
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
    MarginConfig marginConfig = pdfImageOptions.getMarginConfig();
    marginConfig.setMarginLeft(marginConfig.getMarginLeft() == null ? .5f : marginConfig.getMarginLeft());
    marginConfig.setMarginRight(marginConfig.getMarginRight() == null ? .5f : marginConfig.getMarginRight());
    marginConfig.setMarginTop(marginConfig.getMarginTop() == null ? .5f : marginConfig.getMarginTop());
    marginConfig.setMarginBottom(marginConfig.getMarginBottom() == null ? .5f : marginConfig.getMarginBottom());

    float marginLeft = marginConfig.getMarginLeft();
    float marginRight = marginConfig.getMarginRight();
    float marginTop = marginConfig.getMarginTop();
    float marginBottom = marginConfig.getMarginBottom();


    Document doc = new Document(isVertical ? PageSize.A4 : PageSize.A4.rotate(), marginLeft, marginRight, marginTop,
        marginBottom);
    PdfWriter.getInstance(doc, outputStream);

    doc.open();
    return getCompressedImagesFromPaths(pdfImageOptions.getImagePaths()).collectList().flatMap(images -> {
      try {
        float x_in_cm = marginLeft;
        float y_in_cm = A4HEIGTH - marginTop - maxHeight;
        boolean isFirstImageInLine = true;
        List<ImagePagination> imagePaginations = new ArrayList<>();
        int imagePaginationsIndex = 0;
        imagePaginations.add(new ImagePagination());
        //List<List<Image>> imagesList = new ArrayList<>();
        int imageListIndex = 0;
        for (Image img : images) {   
          boolean wasRotated = setRotationOfImage(img, maxHeight, maxWidth);
          float separator = isFirstImageInLine ? 0 : SEPARATOR;
          float totalWidth = separator + maxWidth;
          if (totalWidth + x_in_cm > (A4WIDTH - marginRight)) {
            x_in_cm = marginLeft;
            y_in_cm = y_in_cm - SEPARATOR - maxHeight;
            isFirstImageInLine = true;
            separator = 0;
            imageListIndex += 1;
          }
          if (y_in_cm - marginBottom < 0) {
            //doc.newPage();
            imagePaginationsIndex++;
            imagePaginations.add(new ImagePagination());
            y_in_cm = A4HEIGTH - marginTop - maxHeight;
            isFirstImageInLine = true;
            separator = 0;
            imageListIndex = 0;
          }
          List<List<Image>> imagesList = imagePaginations.get(imagePaginationsIndex).getImagesList();
          if (imageListIndex == imagesList.size()) {
            imagesList.add(new ArrayList<>());
          }
          imagesList.get(imageListIndex).add(img);
          this.setImageOnDoc(doc, img, maxWidth, maxHeight,
              x_in_cm + separator,
              y_in_cm, pdfImageOptions.isForceSize(), wasRotated);
          
          x_in_cm += pdfImageOptions.getMaxWidth() + separator;
          isFirstImageInLine = false;
        }

        centerImagesvertically(imagePaginations, A4HEIGTH, maxHeight, SEPARATOR, marginConfig);
        centerImagesHorizontally(imagePaginations, A4WIDTH, maxWidth, SEPARATOR, marginConfig);
        imagePaginations.forEach(imagePagination -> {
          imagePagination.getImagesList().forEach(imagesList -> {
            imagesList.forEach(image -> {
              doc.add(image);
            });
          });
          doc.newPage();
        });
        doc.close();
        ByteArrayResource bytesArrayRes = new ByteArrayResource(outputStream.toByteArray());
        return Mono.just(bytesArrayRes);
      } catch (Exception e) {
        return Mono.error(e);
      }
    });
  }
  private void centerImagesHorizontally(List<ImagePagination> imagePaginations, float totalWidthInCm, float maxWidthInCm, float separatorInCm, MarginConfig marginConfig) {
    imagePaginations.forEach(imagePagination -> {
      imagePagination.getImagesList().forEach(row -> {
        int n_column = row.size();
        float separatorSize = (n_column - 1) * pdfMakerTools.cmToPoints(separatorInCm);
        float total = separatorSize + (n_column * pdfMakerTools.cmToPoints(maxWidthInCm));
        float marginToSubtract = pdfMakerTools.cmToPoints(marginConfig.getMarginLeft() + marginConfig.getMarginRight());
        float rest = pdfMakerTools.cmToPoints(totalWidthInCm) - total - marginToSubtract;
        float sizeToMoveInTotalContext = rest / 2;
        row.forEach(image -> {
          float x = image.getAbsoluteX();
          float y = image.getAbsoluteY();
          float width = image.getPlainWidth();
          float restInTheWidthContainer = pdfMakerTools.cmToPoints(maxWidthInCm) - width;
          float sizeToMoveInImageContainer = restInTheWidthContainer / 2;
          image.setAbsolutePosition(x + sizeToMoveInTotalContext + sizeToMoveInImageContainer, y);
        });
      });
    });
  }
  private void centerImagesvertically(List<ImagePagination> imagePaginations, float totalHeightInCm, float maxHeightInCm, float separatorInCm, MarginConfig marginConfig) {
    imagePaginations.forEach(imagePagination -> {
      int n_rows = imagePagination.getImagesList().size();
      float separatorsSize = (n_rows - 1) * pdfMakerTools.cmToPoints(separatorInCm);
      float total = separatorsSize + (n_rows * pdfMakerTools.cmToPoints(maxHeightInCm));
      System.out.println("separators Size: " + separatorsSize);
      System.out.println("total : " + total);

      float marginToSubtract = pdfMakerTools.cmToPoints(marginConfig.getMarginTop() + marginConfig.getMarginBottom());
      float rest = pdfMakerTools.cmToPoints(totalHeightInCm) - total - marginToSubtract;
      float sizeToMoveInTotalContext = rest / 2;
      imagePagination.getImagesList().forEach(imageRow->{
        imageRow.forEach(image ->{ 
          float x = image.getAbsoluteX();
          float y = image.getAbsoluteY();
          float height = image.getPlainHeight();
          float restInTheHeightContainer = pdfMakerTools.cmToPoints(maxHeightInCm) - height;
          float sizeToMoveInImageContainer = restInTheHeightContainer / 2;
          image.setAbsolutePosition(x, y  - sizeToMoveInTotalContext + sizeToMoveInImageContainer);
        });
      });
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
    float maxHeightInPoints = pdfMakerTools.cmToPoints(maxHeigth);

    if (forceSize) {
      img.scaleAbsolute(wasRotated ? maxHeightInPoints : maxWidthInPoints, wasRotated ? maxWidthInPoints : maxHeightInPoints);
    } else {
      img.scaleToFit(maxWidthInPoints, maxHeightInPoints);
    }
    img.setAbsolutePosition(pdfMakerTools.cmToPoints(x), pdfMakerTools.cmToPoints(y));
    //doc.add(img);
  }
}
