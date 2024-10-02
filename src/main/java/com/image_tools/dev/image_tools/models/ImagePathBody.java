package com.image_tools.dev.image_tools.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImagePathBody {
    private String path;
    private Integer numberOfCopies;
}
