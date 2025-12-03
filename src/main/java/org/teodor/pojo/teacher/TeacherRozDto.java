package org.teodor.pojo.teacher;

import org.teodor.pojo.classes.LessonDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Represents the "roz" block
// Map<DayNumberString, Map<LessonNumberString, List<Lesson>>>
public class TeacherRozDto extends HashMap<String, Map<String, List<TeacherLessonDto>>> {
    // No additional code needed here. It inherits Map functionality.
    // Jackson can handle the nested structure directly via the type signature.
}