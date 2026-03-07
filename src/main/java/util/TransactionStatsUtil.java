package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import model.Player;
import model.Position;
import model.Transaction;

/**
 * Utility class for calculating statistics from transaction data. Works with domain models to
 * provide shared methods for analyzing trades, waivers, and other transaction types.
 */
public class TransactionStatsUtil {

  // Constants
  private static final int TOP_PLAYERS_LIMIT = 10;
  private static final String PICK_PLAYER_ID = "0";
  private static final int MIN_SIGNIFICANT_AMOUNT = 1;

  /**
   * Filter transactions by type and status.
   *
   * @param transactions list of all transactions
   * @param type the transaction type to filter by
   * @return list of completed transactions of the specified type
   */
  public static List<Transaction> filterTransactionsByType(
      List<Transaction> transactions, Transaction.Type type) {
    return transactions.stream()
        .filter(t -> t.getStatus() == Transaction.Status.COMPLETE)
        .filter(t -> t.getType() == type)
        .collect(Collectors.toList());
  }

  /**
   * Get the most involved players across all positions.
   *
   * <p>Returns a map where keys are Position enums and values are maps of player names to counts.
   *
   * @param nflPlayers map of all NFL players
   * @param transactions list of transactions
   * @return map of positions to player involvement counts
   */
  public static Map<Position, Map<String, Long>> getMostInvolvedPlayersByAllPositions(
      Map<String, Player> nflPlayers, List<Transaction> transactions) {

    Map<Position, Map<String, Long>> result = new HashMap<>();
    result.put(
        Position.RB, getMostInvolvedPlayersByPosition(nflPlayers, transactions, Position.RB));
    result.put(
        Position.QB, getMostInvolvedPlayersByPosition(nflPlayers, transactions, Position.QB));
    result.put(
        Position.WR, getMostInvolvedPlayersByPosition(nflPlayers, transactions, Position.WR));
    result.put(
        Position.TE, getMostInvolvedPlayersByPosition(nflPlayers, transactions, Position.TE));
    return result;
  }

  /**
   * Get the most frequently involved players across all transactions.
   *
   * @param nflPlayers map of all NFL players
   * @param transactions list of transactions
   * @return map of player names to transaction counts, sorted by count descending, limited to top
   *     10
   */
  public static Map<String, Long> getMostInvolvedPlayers(
      Map<String, Player> nflPlayers, List<Transaction> transactions) {
    Map<String, Long> playerCountMap =
        transactions.stream()
            .flatMap(transaction -> transaction.getAdds().keySet().stream())
            .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

    // Pre-collect involved player IDs for efficient filtering
    Set<String> involvedPlayerIds = playerCountMap.keySet();

    return nflPlayers.values().stream()
        .filter(p -> !p.getPlayerId().equalsIgnoreCase(PICK_PLAYER_ID))
        .filter(p -> involvedPlayerIds.contains(p.getPlayerId()))
        .collect(
            Collectors.toMap(
                Player::getName, player -> playerCountMap.getOrDefault(player.getPlayerId(), 0L)))
        .entrySet()
        .stream()
        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Get the most frequently involved players for a specific position.
   *
   * @param nflPlayers map of all NFL players
   * @param transactions list of transactions
   * @param position the position to filter by
   * @return map of player names to transaction counts, sorted by count descending, limited to top
   *     10
   */
  public static Map<String, Long> getMostInvolvedPlayersByPosition(
      Map<String, Player> nflPlayers, List<Transaction> transactions, Position position) {

    Map<String, Long> playerCountMap =
        transactions.stream()
            .flatMap(transaction -> transaction.getAdds().keySet().stream())
            .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

    // Pre-collect involved player IDs for efficient filtering
    Set<String> involvedPlayerIds = playerCountMap.keySet();

    return nflPlayers.values().stream()
        .filter(p -> !p.getPlayerId().equalsIgnoreCase(PICK_PLAYER_ID))
        .filter(p -> p.getPosition() == position)
        .filter(p -> involvedPlayerIds.contains(p.getPlayerId()))
        .collect(
            Collectors.toMap(
                Player::getName, player -> playerCountMap.getOrDefault(player.getPlayerId(), 0L)))
        .entrySet()
        .stream()
        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Checks if a transaction is significant (not a low-value transaction).
   *
   * <p>Filters out transactions with waiver budget amounts of 1 or less, which are typically
   * considered "bean trades" or low-value transactions.
   *
   * @param transaction the transaction to check
   * @return true if the transaction is significant (empty waiver budget or all amounts > 1)
   */
  public static boolean isSignificantTransaction(Transaction transaction) {
    return transaction.getWaiverBudget().isEmpty()
        || transaction.getWaiverBudget().stream()
            .allMatch(budget -> budget.getAmount() > MIN_SIGNIFICANT_AMOUNT);
  }

  /**
   * Count transactions by position in a single pass for efficiency.
   *
   * @param transactions the list of transactions to analyze
   * @param rosterId the roster ID to check for player additions
   * @param runningBackIds list of RB player IDs
   * @param quarterBackIds list of QB player IDs
   * @param wideReceiverIds list of WR player IDs
   * @param tightEndIds list of TE player IDs
   * @return map with keys "RB", "QB", "WR", "TE" and their respective counts
   */
  public static Map<String, Long> countTransactionsByPosition(
      List<Transaction> transactions,
      int rosterId,
      List<String> runningBackIds,
      List<String> quarterBackIds,
      List<String> wideReceiverIds,
      List<String> tightEndIds) {

    Map<String, Long> counts = new HashMap<>();
    counts.put("RB", 0L);
    counts.put("QB", 0L);
    counts.put("WR", 0L);
    counts.put("TE", 0L);

    for (Transaction transaction : transactions) {
      for (Map.Entry<String, Integer> add : transaction.getAdds().entrySet()) {
        if (add.getValue() == rosterId) {
          String playerId = add.getKey();
          if (runningBackIds.contains(playerId)) {
            counts.put("RB", counts.get("RB") + 1);
          } else if (quarterBackIds.contains(playerId)) {
            counts.put("QB", counts.get("QB") + 1);
          } else if (wideReceiverIds.contains(playerId)) {
            counts.put("WR", counts.get("WR") + 1);
          } else if (tightEndIds.contains(playerId)) {
            counts.put("TE", counts.get("TE") + 1);
          }
        }
      }
    }

    return counts;
  }

  /**
   * Extract player IDs grouped by position from a collection of players.
   *
   * @param players collection of NFL players
   * @return map of position to list of player IDs for that position
   */
  public static Map<Position, List<String>> getPlayerIdsByPosition(Collection<Player> players) {
    Map<Position, List<String>> result = new HashMap<>();
    result.put(Position.RB, Player.getPlayerIdsByPosition(players, Position.RB));
    result.put(Position.QB, Player.getPlayerIdsByPosition(players, Position.QB));
    result.put(Position.WR, Player.getPlayerIdsByPosition(players, Position.WR));
    result.put(Position.TE, Player.getPlayerIdsByPosition(players, Position.TE));
    return result;
  }

  /**
   * Calculate the most dropped players from free agent and waiver transactions.
   *
   * @param nflPlayers map of all NFL players
   * @param transactions list of all transactions
   * @return map of player names to drop counts, sorted by count descending, limited to top 10
   */
  public static Map<String, Long> getMostDroppedPlayers(
      Map<String, Player> nflPlayers, List<Transaction> transactions) {
    List<Transaction> drops =
        transactions.stream()
            .filter(drop -> drop.getDrops() != null)
            .filter(x -> x.getStatus() == Transaction.Status.COMPLETE)
            .filter(
                x ->
                    x.getType() == Transaction.Type.FREE_AGENT
                        || x.getType() == Transaction.Type.WAIVER)
            .toList();

    Map<String, Long> dropsCountMap =
        drops.stream()
            .flatMap(drop -> drop.getDrops().keySet().stream())
            .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

    // Pre-collect dropped player IDs for efficient filtering
    Set<String> droppedPlayerIds = dropsCountMap.keySet();

    return nflPlayers.values().stream()
        .filter(p -> !p.getPlayerId().equalsIgnoreCase(PICK_PLAYER_ID))
        .filter(p -> droppedPlayerIds.contains(p.getPlayerId()))
        .collect(
            Collectors.toMap(
                Player::getName, player -> dropsCountMap.getOrDefault(player.getPlayerId(), 0L)))
        .entrySet()
        .stream()
        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
        .limit(TOP_PLAYERS_LIMIT)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }
}
