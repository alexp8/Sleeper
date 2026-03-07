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
import model.report.BeansStats;
import model.report.TradeReport;
import model.report.UserTradeStats;
import util.PositionIds;
import util.TransactionStatsUtil;

@Slf4j
public class CalcTrades {

  /**
   * Calculate trade statistics and generate a comprehensive trade report.
   *
   * @param rosters list of all rosters
   * @param nflPlayers map of all NFL players
   * @param transactions list of all transactions
   * @param users list of all users
   * @return TradeReport containing all trade statistics
   */
  public static TradeReport calcTrades(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Transaction> transactions,
      List<User> users) {

    log.info("Beginning calcTrades");

    // Extract position IDs
    PositionIds positionIds = PositionIds.from(nflPlayers.values());

    // Filter completed trades
    List<Transaction> trades =
        TransactionStatsUtil.filterTransactionsByType(transactions, Transaction.Type.TRADE);

    // Collect user trade statistics
    List<UserTradeStats> userTradeStatsList = new ArrayList<>();
    for (User user : users) {

      // Find this user's roster
      Roster roster = Roster.getUserRoster(rosters, user.getUserId());

      // Find trades involving this roster
      List<Transaction> userTrades =
          trades.stream()
              .filter(t -> t.getConsenterIds().contains((long) roster.getRosterId()))
              .filter(TransactionStatsUtil::isSignificantTransaction)
              .collect(Collectors.toList());

      // Count trades by position in a single pass
      Map<String, Long> positionTradeCounts =
          TransactionStatsUtil.countTransactionsByPosition(
              userTrades,
              roster.getRosterId(),
              positionIds.getRunningBackIds(),
              positionIds.getQuarterBackIds(),
              positionIds.getWideReceiverIds(),
              positionIds.getTightEndIds());

      // Build user trade stats object
      userTradeStatsList.add(
          UserTradeStats.builder()
              .userName(user.getName())
              .userId(user.getUserId())
              .rosterId(roster.getRosterId())
              .totalTrades(userTrades.size())
              .tradedForRbs(positionTradeCounts.get("RB"))
              .tradedForQbs(positionTradeCounts.get("QB"))
              .tradedForWrs(positionTradeCounts.get("WR"))
              .tradedForTes(positionTradeCounts.get("TE"))
              .build());
    }

    // Calculate most traded players overall
    Map<String, Long> mostTradedPlayers =
        TransactionStatsUtil.getMostInvolvedPlayers(nflPlayers, trades);

    // Calculate most dropped players
    Map<String, Long> mostDroppedPlayers =
        TransactionStatsUtil.getMostDroppedPlayers(nflPlayers, transactions);

    // Calculate most traded players by position
    Map<Position, Map<String, Long>> mostTradedByPosition =
        TransactionStatsUtil.getMostInvolvedPlayersByAllPositions(nflPlayers, trades);

    Map<String, Long> mostTradedRbs = mostTradedByPosition.get(Position.RB);
    Map<String, Long> mostTradedQbs = mostTradedByPosition.get(Position.QB);

    // Most beans spent in trades
    List<BeansStats> beansStatsList = getBeansStats(trades, users, rosters);

    // Build complete report
    return TradeReport.builder()
        .generatedAt(LocalDateTime.now())
        .totalTrades(trades.size())
        .userTradeStats(userTradeStatsList)
        .mostTradedPlayers(mostTradedPlayers)
        .mostDroppedPlayers(mostDroppedPlayers)
        .mostTradedRbs(mostTradedRbs)
        .mostTradedQbs(mostTradedQbs)
        .beansStats(beansStatsList)
        .build();
  }

  private static List<BeansStats> getBeansStats(
      List<Transaction> trades, List<User> users, List<Roster> rosters) {

    List<BeansStats> beansStatsList = new ArrayList<>();

    for (User user : users) {

      // Find this user's roster
      Roster roster = Roster.getUserRoster(rosters, user.getUserId());

      int beansReceivedFromTrades =
          trades.stream()
              .filter(x -> x.getRosterIds().contains((long) roster.getRosterId()))
              .filter(t -> t.getWaiverBudget() != null)
              .flatMap(t -> t.getWaiverBudget().stream())
              .filter(w -> w.getReceiver() == roster.getRosterId())
              .mapToInt(Transaction.WaiverBudget::getAmount)
              .sum();

      int beansSpentInTrades =
          trades.stream()
              .filter(x -> x.getRosterIds().contains((long) roster.getRosterId()))
              .filter(t -> t.getWaiverBudget() != null)
              .flatMap(t -> t.getWaiverBudget().stream())
              .filter(w -> w.getSender() == roster.getRosterId())
              .mapToInt(Transaction.WaiverBudget::getAmount)
              .sum();

      beansStatsList.add(
          BeansStats.builder()
              .userName(user.getName())
              .userId(user.getUserId())
              .rosterId(roster.getRosterId())
              .beansReceived(beansReceivedFromTrades)
              .beansSpent(beansSpentInTrades)
              .netBeans(beansReceivedFromTrades - beansSpentInTrades)
              .build());
    }

    return beansStatsList;
  }
}
