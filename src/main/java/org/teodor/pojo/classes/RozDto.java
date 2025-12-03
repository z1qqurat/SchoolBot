package org.teodor.pojo.classes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Represents the "roz" block
// Map<DayNumberString, Map<LessonNumberString, List<Lesson>>>
public class RozDto extends HashMap<String, Map<String, List<LessonDto>>> {
    // No additional code needed here. It inherits Map functionality.
    // Jackson can handle the nested structure directly via the type signature.
}