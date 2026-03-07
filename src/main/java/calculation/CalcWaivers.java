package calculation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import model.Player;
import model.Position;
import model.Roster;
import model.Transaction;
import model.User;
import model.report.UserWaiverStats;
import model.report.WaiverReport;
import util.PositionIds;
import util.TransactionStatsUtil;

@Slf4j
public class CalcWaivers {

  /**
   * Calculate waiver statistics and generate a comprehensive waiver report.
   *
   * @param rosters list of all rosters
   * @param nflPlayers map of all NFL players
   * @param transactions list of all transactions
   * @param users list of all users
   * @return WaiverReport containing all waiver statistics
   */
  public static WaiverReport calcWaivers(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Transaction> transactions,
      List<User> users) {

    log.info("Beginning calcWaivers");

    // Extract position IDs
    PositionIds positionIds = PositionIds.from(nflPlayers.values());

    // Filter completed waivers
    List<Transaction> waivers =
        TransactionStatsUtil.filterTransactionsByType(transactions, Transaction.Type.WAIVER);

    // Collect user waiver statistics
    List<UserWaiverStats> userWaiverStatsList = new ArrayList<>();
    for (User user : users) {

      // Find this user's roster
      Roster roster = Roster.getUserRoster(rosters, user.getUserId());

      // Find waivers involving this roster, excluding low-value transactions
      List<Transaction> userWaivers =
          waivers.stream()
              .filter(w -> w.getConsenterIds().contains((long) roster.getRosterId()))
              .filter(TransactionStatsUtil::isSignificantTransaction)
              .collect(Collectors.toList());

      // Count waivers by position in a single pass
      Map<String, Long> positionWaiverCounts =
          TransactionStatsUtil.countTransactionsByPosition(
              userWaivers,
              roster.getRosterId(),
              positionIds.getRunningBackIds(),
              positionIds.getQuarterBackIds(),
              positionIds.getWideReceiverIds(),
              positionIds.getTightEndIds());

      long waiversForRbs = positionWaiverCounts.get("RB");
      long waiversForQbs = positionWaiverCounts.get("QB");
      long waiversForWrs = positionWaiverCounts.get("WR");
      long waiversForTes = positionWaiverCounts.get("TE");

      int totalFabSpent =
          userWaivers.stream()
              .filter(waiver -> waiver.getSettings() != null)
              .mapToInt(waiver -> waiver.getSettings().getWaiverBid())
              .sum();

      // Build user waiver stats object
      userWaiverStatsList.add(
          UserWaiverStats.builder()
              .userName(user.getName())
              .userId(user.getUserId())
              .rosterId(roster.getRosterId())
              .totalWaivers(userWaivers.size())
              .waiveredForRbs(waiversForRbs)
              .waiveredForQbs(waiversForQbs)
              .waiveredForWrs(waiversForWrs)
              .waiveredForTes(waiversForTes)
              .totalFabSpent(totalFabSpent)
              .build());
    }

    // Calculate most waivered players overall
    Map<String, Long> mostWaiveredPlayers =
        TransactionStatsUtil.getMostInvolvedPlayers(nflPlayers, waivers);

    // Calculate most waivered players by position
    Map<Position, Map<String, Long>> mostWaiveredByPosition =
        TransactionStatsUtil.getMostInvolvedPlayersByAllPositions(nflPlayers, waivers);

    Map<String, Long> mostWaiveredRbs = mostWaiveredByPosition.get(Position.RB);
    Map<String, Long> mostWaiveredQbs = mostWaiveredByPosition.get(Position.QB);
    Map<String, Long> mostWaiveredWrs = mostWaiveredByPosition.get(Position.WR);
    Map<String, Long> mostWaiveredTes = mostWaiveredByPosition.get(Position.TE);

    // Build complete report
    return WaiverReport.builder()
        .generatedAt(LocalDateTime.now())
        .totalWaivers(waivers.size())
        .userWaiverStats(userWaiverStatsList)
        .mostWaiveredPlayers(mostWaiveredPlayers)
        .mostWaiveredRbs(mostWaiveredRbs)
        .mostWaiveredQbs(mostWaiveredQbs)
        .mostWaiveredWrs(mostWaiveredWrs)
        .mostWaiveredTes(mostWaiveredTes)
        .build();
  }
}
