package model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Matchup data from Sleeper API. Pure data transfer object with no business logic, only
 * Jackson mapping.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchupDto {

  private List<String> starters;

  @JsonProperty("roster_id")
  private int rosterId;

  private List<String> players;

  private double points;

  @JsonProperty("players_points")
  private Map<String, Double> playerPoints;

  @JsonProperty("starters_points")
  private List<Double> starterPoints;

  @JsonProperty("matchup_id")
  private int matchupId;
}
