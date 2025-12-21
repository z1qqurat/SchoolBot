package org.teodor.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class UserDTO {
    private Long id;
    private String name;
    @JsonProperty("is_teacher")
    private boolean isTeacher;
    @JsonProperty("tracking_id")
    private String trackingId;
    @JsonProperty("is_notification")
    private boolean isNotification;
}