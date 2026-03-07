package model;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a fantasy football roster. Contains business logic for roster
 * operations.
 */
@Data
@Builder
public class Roster {
  private int rosterId;
  private String ownerId;
  private List<String> players;

  /**
   * Find a roster by user ID.
   *
   * @param rosters list of all rosters
   * @param userId the user ID to search for
   * @return the Roster owned by the specified user
   * @throws NoSuchElementException if no roster is found for the user
   */
  public static Roster getUserRoster(List<Roster> rosters, String userId) {
    return rosters.stream()
        .filter(r -> r.getOwnerId().equalsIgnoreCase(userId))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Failed to find roster for user: " + userId));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Roster roster = (Roster) o;
    return rosterId == roster.rosterId && Objects.equals(ownerId, roster.ownerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rosterId, ownerId);
  }
}
