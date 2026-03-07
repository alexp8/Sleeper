package model.report;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Top-level league overview report containing league metadata and links to all reports. */
@Data
@Builder
public class LeagueReport {
  private LocalDateTime generatedAt;
  private String leagueName;
  private String currentSeason;
  private String seasonType;
  private String status;
  private int totalRosters;
  private List<Integer> years;
  private List<String> members;
  private int totalMembers;
  private String earliestYear;
  private String latestYear;
}
