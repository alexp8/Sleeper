package model;

import lombok.Builder;
import lombok.Data;

/** Domain model representing a fantasy football league. */
@Data
@Builder
public class League {
  private String leagueId;
  private String name;
  private String season;
  private String seasonType;
  private String status;
  private String sport;
  private int totalRosters;
  private String avatar;
  private String previousLeagueId;
  private String draftId;
}
