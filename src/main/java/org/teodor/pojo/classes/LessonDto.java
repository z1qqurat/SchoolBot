package org.teodor.pojo.classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long sid;
    private Integer pn;
    private Integer p;
    private String um;
    private Integer g;
    private List<NumDto> nums;
}