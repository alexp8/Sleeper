package calculation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.Matchup;
import model.Player;
import model.Position;
import model.Roster;
import model.User;
import model.report.MatchupReport;
import model.report.UserMatchupStats;
import util.PositionIds;

@Slf4j
public class CalcMatchups {

  // Constants
  private static final int CLOSE_MATCHUP_THRESHOLD = 10;
  private static final int TOP_PLAYERS_LIMIT = 10;

  /**
   * Calculate matchup statistics and generate a comprehensive matchup report.
   *
   * @param rosters list of all rosters
   * @param nflPlayers map of all NFL players
   * @param matchups list of all matchups
   * @param users list of all users
   * @return MatchupReport containing all matchup statistics
   */
  public static MatchupReport calcMatchups(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Matchup> matchups,
      List<User> users) {

    log.info("Beginning calcMatchups");

    // Extract position IDs
    PositionIds positionIds = PositionIds.from(nflPlayers.values());

    // Calculate user matchup statistics (includes bench points)
    List<UserMatchupStats> userMatchupStats =
        calculateUserMatchupStats(rosters, matchups, users, positionIds);

    // Calculate peak player statistics
    MatchupReport.PeakPlayerStats peakPlayerStats = calculatePeakPlayerStats(nflPlayers, matchups);

    // Calculate close matchup statistics
    CloseMatchupResults closeMatchupResults = calculateCloseMatchups(matchups, users, rosters);

    // Build complete report
    return MatchupReport.builder()
        .generatedAt(LocalDateTime.now())
        .totalMatchups(matchups.size())
        .userMatchupStats(userMatchupStats)
        .peakPlayerStats(peakPlayerStats)
        .closestLoss(closeMatchupResults.closestLoss)
        .closeLossCounts(closeMatchupResults.closeLossCounts)
        .build();
  }

  /** Helper class to hold close matchup calculation results */
  private static class CloseMatchupResults {
    MatchupReport.ClosestMatchupInfo closestLoss;
    Map<String, Integer> closeLossCounts;

    CloseMatchupResults(
        MatchupReport.ClosestMatchupInfo closestLoss, Map<String, Integer> closeLossCounts) {
      this.closestLoss = closestLoss;
      this.closeLossCounts = closeLossCounts;
    }
  }

  /**
   * Calculate bench points for a user across all matchups.
   *
   * @param matchups all matchups
   * @param roster user's roster
   * @return total points scored by bench players
   */
  private static double calculateBenchPoints(List<Matchup> matchups, Roster roster) {
    return matchups.stream()
        .filter(matchup -> matchup.getPoints() > 0)
        .filter(matchup -> matchup.getRosterId() == roster.getRosterId())
        .flatMap(
            matchup ->
                matchup.getPlayerPoints().entrySet().stream()
                    .filter(entry -> !matchup.getStarters().contains(entry.getKey()))
                    .map(Map.Entry::getValue))
        .mapToDouble(Double::doubleValue)
        .sum();
  }

  /**
   * Calculate close matchup statistics including closest loss and count of close losses.
   *
   * @return CloseMatchupResults containing closest loss info and close loss counts
   */
  private static CloseMatchupResults calculateCloseMatchups(
      List<Matchup> matchups, List<User> users, List<Roster> rosters) {

    if (matchups.isEmpty()) {
      log.warn("No matchups available for close matchup analysis");
      return new CloseMatchupResults(null, new HashMap<>());
    }

    // Find closest loss
    MatchupReport.ClosestMatchupInfo closestLoss = findClosestLoss(matchups, users, rosters);

    // Count close losses per user
    Map<String, Integer> closeLossCounts = countCloseLosses(matchups, users, rosters);

    return new CloseMatchupResults(closestLoss, closeLossCounts);
  }

  /**
   * Find and log the closest loss across all matchups.
   *
   * @param matchups list of all matchups
   * @param users list of users
   * @param rosters list of rosters
   * @return ClosestMatchupInfo with user and point difference, or null if no matchups
   */
  private static MatchupReport.ClosestMatchupInfo findClosestLoss(
      List<Matchup> matchups, List<User> users, List<Roster> rosters) {

    Matchup loserWithClosestLoss = null;
    double minPointDiff = Double.MAX_VALUE;

    for (Matchup matchup : matchups) {
      Matchup otherMatchup =
          matchups.stream()
              .filter(m -> m.getMatchupId() == matchup.getMatchupId())
              .filter(m -> m.getRosterId() != matchup.getRosterId())
              .findFirst()
              .orElseThrow(
                  () -> new NoSuchElementException("Failed to find other matchup for: " + matchup));

      double pointDiff = Math.abs(matchup.getPoints() - otherMatchup.getPoints());
      Matchup loser = matchup.getPoints() < otherMatchup.getPoints() ? matchup : otherMatchup;

      if (pointDiff < minPointDiff) {
        minPointDiff = pointDiff;
        loserWithClosestLoss = loser;
      }
    }

    if (loserWithClosestLoss != null) {
      User userLoser = User.getUserFromMatchup(loserWithClosestLoss, rosters, users);

      return MatchupReport.ClosestMatchupInfo.builder()
          .userName(userLoser.getName())
          .pointDifference(minPointDiff)
          .build();
    }

    return null;
  }

  /**
   * Count and log losses within the close matchup threshold for each user.
   *
   * @param matchups list of all matchups
   * @param users list of users
   * @param rosters list of rosters
   * @return map of user names to count of close losses
   */
  private static Map<String, Integer> countCloseLosses(
      List<Matchup> matchups, List<User> users, List<Roster> rosters) {

    Map<String, Integer> closeLossesPerUser = new HashMap<>();

    for (Matchup matchup : matchups) {
      Matchup otherMatchup =
          matchups.stream()
              .filter(m -> m.getMatchupId() == matchup.getMatchupId())
              .filter(m -> m.getRosterId() != matchup.getRosterId())
              .findFirst()
              .orElseThrow(
                  () -> new NoSuchElementException("Failed to find other matchup for: " + matchup));

      double pointDiff = Math.abs(matchup.getPoints() - otherMatchup.getPoints());

      // Track close losses
      if (pointDiff < CLOSE_MATCHUP_THRESHOLD) {
        User user = User.getUserFromMatchup(matchup, rosters, users);
        String userName = user.getName();
        int losses = closeLossesPerUser.getOrDefault(userName, 0);
        closeLossesPerUser.put(userName, losses + 1);
      }
    }

    return closeLossesPerUser;
  }

  /**
   * Calculate comprehensive matchup statistics for all users.
   *
   * @return list of user matchup statistics
   */
  private static List<UserMatchupStats> calculateUserMatchupStats(
      List<Roster> rosters, List<Matchup> matchups, List<User> users, PositionIds positionIds) {

    List<UserMatchupStats> userStatsList = new ArrayList<>();

    // for every user, grab how many points they have scored for each position on a starting lineup
    for (User user : users) {

      // find this user's roster
      Roster roster = Roster.getUserRoster(rosters, user.getUserId());

      // find player matchups
      List<Matchup> userMatchups =
          matchups.stream()
              .filter(matchup -> matchup.getPoints() > 0)
              .filter(matchup -> matchup.getRosterId() == roster.getRosterId())
              .toList();

      // Calculate all position points in a single pass
      PositionPoints positionPoints = calculatePositionPoints(userMatchups, positionIds);

      double totalPoints = userMatchups.stream().mapToDouble(Matchup::getPoints).sum();

      // numDonuts
      int numDonuts = countDonuts(userMatchups);

      // Calculate bench points
      double totalBenchPoints = calculateBenchPoints(matchups, roster);

      // Build user stats object for report
      UserMatchupStats stats =
          UserMatchupStats.builder()
              .userName(user.getName())
              .userId(user.getUserId())
              .rosterId(roster.getRosterId())
              .totalPoints(totalPoints)
              .rbPoints(positionPoints.rbPoints)
              .wrPoints(positionPoints.wrPoints)
              .tePoints(positionPoints.tePoints)
              .qbPoints(positionPoints.qbPoints)
              .numDonuts(numDonuts)
              .totalBenchPoints(totalBenchPoints)
              .build();
      userStatsList.add(stats);

      // Also keep metrics for logging top performers
      Metric metric =
          Metric.builder()
              .name(user.getName())
              .rbPoints(positionPoints.rbPoints)
              .wrPoints(positionPoints.wrPoints)
              .tePoints(positionPoints.tePoints)
              .qbPoints(positionPoints.qbPoints)
              .totalPoints(totalPoints)
              .numDonuts(numDonuts)
              .build();
    }

    return userStatsList;
  }

  /**
   * Calculate points for all positions in a single pass through matchups.
   *
   * @return PositionPoints containing cumulative points for each position
   */
  private static PositionPoints calculatePositionPoints(
      List<Matchup> userMatchups, PositionIds positionIds) {

    double rbPoints = 0;
    double qbPoints = 0;
    double wrPoints = 0;
    double tePoints = 0;

    for (Matchup matchup : userMatchups) {
      for (Map.Entry<String, Double> playerPoint : matchup.getPlayerPoints().entrySet()) {
        String playerId = playerPoint.getKey();

        // Only count starters
        if (!matchup.getStarters().contains(playerId)) {
          continue;
        }

        double points = playerPoint.getValue();
        if (positionIds.getRunningBackIds().contains(playerId)) {
          rbPoints += points;
        } else if (positionIds.getQuarterBackIds().contains(playerId)) {
          qbPoints += points;
        } else if (positionIds.getWideReceiverIds().contains(playerId)) {
          wrPoints += points;
        } else if (positionIds.getTightEndIds().contains(playerId)) {
          tePoints += points;
        }
      }
    }

    return new PositionPoints(rbPoints, qbPoints, wrPoints, tePoints);
  }

  /** Helper class to hold position points */
  private record PositionPoints(
      double rbPoints, double qbPoints, double wrPoints, double tePoints) {}

  /**
   * Count the number of matchups where user had at least one starter score 0 points.
   *
   * @param userMatchups list of user's matchups
   * @return count of "donuts" (matchups with 0-point starters)
   */
  private static int countDonuts(List<Matchup> userMatchups) {
    return (int)
        userMatchups.stream()
            .filter(
                matchup -> {
                  return matchup.getPlayerPoints().entrySet().stream()
                      .filter(player -> matchup.getStarters().contains(player.getKey()))
                      .anyMatch(player -> player.getValue() == 0);
                })
            .count();
  }

  /**
   * Calculate weekly peak points by iterating matchups once instead of all players. This is much
   * more efficient: O(matchups) instead of O(players × matchups).
   *
   * @return PeakPlayerStats containing top performers in various categories
   */
  private static MatchupReport.PeakPlayerStats calculatePeakPlayerStats(
      Map<String, Player> nflPlayers, List<Matchup> matchups) {

    // Track player statistics by building up from matchups
    Map<String, PlayerStats> playerStatsMap = new HashMap<>();

    // Single pass through matchups to build player statistics
    for (Matchup matchup : matchups) {
      if (matchup.getPoints() <= 0) {
        continue;
      }

      for (Map.Entry<String, Double> playerPoint : matchup.getPlayerPoints().entrySet()) {
        String playerId = playerPoint.getKey();

        // Only track starters
        if (!matchup.getStarters().contains(playerId)) {
          continue;
        }

        double points = playerPoint.getValue();

        PlayerStats stats = playerStatsMap.computeIfAbsent(playerId, k -> new PlayerStats());
        stats.maxPoints = Math.max(stats.maxPoints, points);
        if (points == 0) {
          stats.numDonuts++;
        }
      }
    }

    // Convert to Starter objects
    List<Starter> starters =
        playerStatsMap.entrySet().stream()
            .filter(entry -> nflPlayers.containsKey(entry.getKey()))
            .map(
                entry -> {
                  String playerId = entry.getKey();
                  PlayerStats stats = entry.getValue();
                  Player player = nflPlayers.get(playerId);

                  return Starter.builder()
                      .name(player.getName())
                      .playerId(playerId)
                      .numDonuts(stats.numDonuts)
                      .maxMatchupPts(stats.maxPoints)
                      .build();
                })
            .collect(Collectors.toList());

    // Build maps for report
    Map<String, Integer> mostDonutPlayers = buildDonutMap(starters);
    Map<String, Double> highestWeeklyPtsPlayers = buildPeakPointsMap(starters);
    Map<String, Double> mostQbPtsPlayers =
        buildPeakPointsByPositionMap(starters, nflPlayers, Position.QB);
    Map<String, Double> mostWrPtsPlayers =
        buildPeakPointsByPositionMap(starters, nflPlayers, Position.WR);
    Map<String, Double> mostTePtsPlayers =
        buildPeakPointsByPositionMap(starters, nflPlayers, Position.TE);
    Map<String, Double> mostRbPtsPlayers =
        buildPeakPointsByPositionMap(starters, nflPlayers, Position.RB);

    return MatchupReport.PeakPlayerStats.builder()
        .mostDonutPlayers(mostDonutPlayers)
        .highestWeeklyPtsPlayers(highestWeeklyPtsPlayers)
        .mostQbPtsPlayers(mostQbPtsPlayers)
        .mostWrPtsPlayers(mostWrPtsPlayers)
        .mostTePtsPlayers(mostTePtsPlayers)
        .mostRbPtsPlayers(mostRbPtsPlayers)
        .build();
  }

  /**
   * Build a map of player names to donut counts, limited to top performers.
   *
   * @param starters list of all starters
   * @return LinkedHashMap preserving order by donut count descending
   */
  private static Map<String, Integer> buildDonutMap(List<Starter> starters) {
    return starters.stream()
        .sorted(Comparator.comparing(Starter::getNumDonuts).reversed())
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Starter::getName, Starter::getNumDonuts, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Build a map of player names to peak points, limited to top performers.
   *
   * @param starters list of all starters
   * @return LinkedHashMap preserving order by points descending
   */
  private static Map<String, Double> buildPeakPointsMap(List<Starter> starters) {
    return starters.stream()
        .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Starter::getName, Starter::getMaxMatchupPts, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Build a map of player names to peak points for a specific position.
   *
   * @param starters list of all starters
   * @param nflPlayers map of all NFL players
   * @param position position to filter by
   * @return LinkedHashMap preserving order by points descending
   */
  private static Map<String, Double> buildPeakPointsByPositionMap(
      List<Starter> starters, Map<String, Player> nflPlayers, Position position) {

    return starters.stream()
        .filter(
            starter ->
                nflPlayers.containsKey(starter.getPlayerId())
                    && nflPlayers.get(starter.getPlayerId()).getPosition() == position)
        .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Starter::getName, Starter::getMaxMatchupPts, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /** Format donut map for logging */
  private static String formatDonutMap(Map<String, Integer> donutMap) {
    return donutMap.entrySet().stream()
        .map(e -> e.getKey() + ": " + e.getValue())
        .collect(Collectors.joining(","));
  }

  /** Format points map for logging */
  private static String formatPointsMap(Map<String, Double> pointsMap) {
    return pointsMap.entrySet().stream()
        .map(e -> e.getKey() + ", pts=" + e.getValue())
        .collect(Collectors.joining(","));
  }

  /** Helper class to track player statistics during matchup iteration */
  private static class PlayerStats {
    int numDonuts = 0;
    double maxPoints = 0.0;
  }

  @Builder
  @Getter
  public static class Starter {
    private final String name;
    private final String playerId;
    private final int numDonuts;
    private final double maxMatchupPts;

    @Override
    public String toString() {
      return "name='" + name + '\'' + ", numDonuts=" + numDonuts;
    }
  }

  @Builder
  @Getter
  public static class Metric {
    private final String name;
    private final double rbPoints;
    private final double tePoints;
    private final double qbPoints;
    private final double wrPoints;
    private final double totalPoints;
    private final int numDonuts;
  }
}
