package org.teodor.pojo.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassDetailsDto {
    private String name;
    private String snum;

    @JsonProperty("min_d")
    private Integer minD;

    @JsonProperty("max_d")
    private Integer maxD;

    @JsonProperty("min_u")
    private Integer minU;

    @JsonProperty("max_u")
    private Integer maxU;

    private RozDto roz;
}
