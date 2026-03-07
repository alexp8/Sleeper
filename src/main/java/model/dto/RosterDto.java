package model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Roster data from Sleeper API. Pure data transfer object with no business logic, only
 * Jackson mapping.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RosterDto {

  @JsonProperty("roster_id")
  private int rosterId;

  @JsonProperty("players")
  private List<String> players;

  @JsonProperty("owner_id")
  private String ownerId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RosterDto rosterDto = (RosterDto) o;
    return rosterId == rosterDto.rosterId && Objects.equals(ownerId, rosterDto.ownerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rosterId, ownerId);
  }
}
