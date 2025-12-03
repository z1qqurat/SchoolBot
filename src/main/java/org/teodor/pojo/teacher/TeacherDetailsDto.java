package org.teodor.pojo.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.teodor.pojo.classes.RozDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDetailsDto {
    private String name;
    private Integer min_d;
    private Integer max_d;
    private Integer min_u;
    private Integer max_u;
    private TeacherRozDto roz;
}
