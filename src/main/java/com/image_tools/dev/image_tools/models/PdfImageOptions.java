package com.image_tools.dev.image_tools.models;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PdfImageOptions {
  private Float maxWidth;
  private Float maxHeight;
  private boolean isVertical;
  private List<String> imagePaths;
  private boolean forceSize;
}
