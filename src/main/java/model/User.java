package model;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a fantasy football league user. Contains business logic for user
 * operations.
 */
@Data
@Builder
public class User {
  private String userId;
  private String name;

  /**
   * Find the user associated with a specific matchup.
   *
   * @param matchup the matchup to find the user for
   * @param rosters list of all rosters in the league
   * @param users list of all users in the league
   * @return the User who owns the roster in the matchup
   * @throws NoSuchElementException if the user cannot be found
   */
  public static User getUserFromMatchup(Matchup matchup, List<Roster> rosters, List<User> users) {
    String userId =
        rosters.stream()
            .filter(r -> r.getRosterId() == matchup.getRosterId())
            .map(Roster::getOwnerId)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Failed to find user id"));

    return users.stream()
        .filter(u -> u.getUserId().equalsIgnoreCase(userId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Failed to find user"));
  }

  @Override
  public String toString() {
    return name;
  }
}
