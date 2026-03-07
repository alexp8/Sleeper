package util;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import model.Player;
import model.Position;

/**
 * Value object that holds player IDs grouped by position.
 *
 * <p>Provides a convenient way to extract and pass around position-specific player lists without
 * repeating the extraction logic.
 */
@Getter
public class PositionIds {
  private final List<String> runningBackIds;
  private final List<String> quarterBackIds;
  private final List<String> wideReceiverIds;
  private final List<String> tightEndIds;

  private PositionIds(
      List<String> runningBackIds,
      List<String> quarterBackIds,
      List<String> wideReceiverIds,
      List<String> tightEndIds) {
    this.runningBackIds = runningBackIds;
    this.quarterBackIds = quarterBackIds;
    this.wideReceiverIds = wideReceiverIds;
    this.tightEndIds = tightEndIds;
  }

  /**
   * Extract position IDs from a collection of players.
   *
   * @param players collection of NFL players
   * @return PositionIds containing lists of player IDs for each position
   */
  public static PositionIds from(Collection<Player> players) {
    return new PositionIds(
        Player.getPlayerIdsByPosition(players, Position.RB),
        Player.getPlayerIdsByPosition(players, Position.QB),
        Player.getPlayerIdsByPosition(players, Position.WR),
        Player.getPlayerIdsByPosition(players, Position.TE));
  }

  /**
   * Get player IDs for a specific position.
   *
   * @param position the position to retrieve
   * @return list of player IDs for that position
   */
  public List<String> getByPosition(Position position) {
    return switch (position) {
      case RB -> runningBackIds;
      case QB -> quarterBackIds;
      case WR -> wideReceiverIds;
      case TE -> tightEndIds;
      default -> throw new IllegalArgumentException("Unsupported position: " + position);
    };
  }
}
