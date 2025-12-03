package org.teodor.pojo.teacher;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherLessonDto {
    private Long sid;
    private Integer pn;
    private List<Integer> cc;
    private String um;
    private Integer p;
    private Integer g;
    private Integer a;
    @JsonProperty("c_s")
    private String cs;
//    private List<TeacherNumDto> nums;
}