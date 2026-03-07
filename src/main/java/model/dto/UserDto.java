package model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for User data from Sleeper API. Pure data transfer object with no business logic, only
 * Jackson mapping.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("display_name")
  private String name;

  @Override
  public String toString() {
    return name;
  }
}
