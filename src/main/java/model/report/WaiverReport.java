package model.report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Top-level waiver report containing all waiver statistics. */
@Data
@Builder
public class WaiverReport {
  private LocalDateTime generatedAt;
  private int totalWaivers;
  private List<UserWaiverStats> userWaiverStats;
  private Map<String, Long> mostWaiveredPlayers;
  private Map<String, Long> mostWaiveredRbs;
  private Map<String, Long> mostWaiveredQbs;
  private Map<String, Long> mostWaiveredWrs;
  private Map<String, Long> mostWaiveredTes;
}
