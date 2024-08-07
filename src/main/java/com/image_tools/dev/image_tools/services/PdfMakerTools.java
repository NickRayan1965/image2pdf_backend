package com.image_tools.dev.image_tools.services;

import org.springframework.stereotype.Service;

@Service
public class PdfMakerTools {
  private static final float CM_TO_POINTS = 72f / 2.54f;
  public float cmToPoints(float cm) {
    return cm * CM_TO_POINTS;
  }
}
