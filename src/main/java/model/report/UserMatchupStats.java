package model.report;

import lombok.Builder;
import lombok.Data;

/**
 * Statistics for a single user's matchup performance across a season.
 *
 * <p>Tracks cumulative points scored by position, total points, donuts (weeks with 0-point
 * starters), and bench points.
 */
@Data
@Builder
public class UserMatchupStats {
  private String userName;
  private String userId;
  private int rosterId;
  private double totalPoints;
  private double rbPoints;
  private double wrPoints;
  private double tePoints;
  private double qbPoints;
  private int numDonuts;
  private double totalBenchPoints;
}
