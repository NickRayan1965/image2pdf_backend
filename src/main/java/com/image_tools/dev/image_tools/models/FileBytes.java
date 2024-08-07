package com.image_tools.dev.image_tools.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileBytes {
  private byte[] bytes;
  private String originalName;
  private String ext;
}
