package com.image_tools.dev.image_tools.models;

import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImagePagination {

    @Builder.Default
    private List<List<Image>> imagesList = new ArrayList<>();
}
