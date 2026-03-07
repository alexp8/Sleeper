package model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

/** Domain model representing an NFL player. Contains business logic for player operations. */
@Data
@Builder
public class Player {
  private String playerId;
  private String firstName;
  private String lastName;
  private Position position;
  private List<Position> fantasyPositions;
  private Integer age;

  /**
   * Get the player's full name.
   *
   * @return full name in "FirstName LastName" format
   */
  public String getName() {
    return firstName + " " + lastName;
  }

  /**
   * Filter players by position and return their IDs.
   *
   * @param players collection of players to filter
   * @param position the position to filter by
   * @return list of player IDs matching the specified position
   */
  public static List<String> getPlayerIdsByPosition(Collection<Player> players, Position position) {
    return players.stream()
        .filter(p -> Objects.equals(p.getPosition(), position))
        .map(Player::getPlayerId)
        .collect(Collectors.toList());
  }
}
