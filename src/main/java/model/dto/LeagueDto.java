package model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** DTO representing the Sleeper API league response. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeagueDto {
  @JsonProperty("league_id")
  private String leagueId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("season")
  private String season;

  @JsonProperty("season_type")
  private String seasonType;

  @JsonProperty("status")
  private String status;

  @JsonProperty("sport")
  private String sport;

  @JsonProperty("total_rosters")
  private int totalRosters;

  @JsonProperty("avatar")
  private String avatar;

  @JsonProperty("previous_league_id")
  private String previousLeagueId;

  @JsonProperty("draft_id")
  private String draftId;
}
