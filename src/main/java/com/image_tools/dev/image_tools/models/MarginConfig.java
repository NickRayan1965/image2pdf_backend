package com.image_tools.dev.image_tools.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarginConfig {
    private Float marginLeft;
    private Float marginRight;
    private Float marginTop;
    private Float marginBottom;
}
