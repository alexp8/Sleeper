package model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a fantasy football matchup. Contains business logic for matchup
 * operations.
 */
@Data
@Builder
public class Matchup {
  private int rosterId;
  private int matchupId;
  private List<String> starters;
  private List<String> players;
  private double points;
  private Map<String, Double> playerPoints;
  private List<Double> starterPoints;
}
