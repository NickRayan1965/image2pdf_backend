package com.image_tools.dev.image_tools.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfImageOptions {
  private Float maxWidth;
  private Float maxHeight;
  private Boolean isVertical;
  private List<ImagePathBody> imagePaths;
  private boolean forceSize;

  @Builder.Default
  private MarginConfig marginConfig = new MarginConfig();
}
