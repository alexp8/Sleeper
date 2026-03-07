package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Domain model representing a fantasy football transaction (trade, waiver, free agent). Contains
 * business logic for transaction operations.
 */
@Data
@Builder
public class Transaction {
  private long statusUpdated;
  private long created;
  private List<Long> rosterIds;
  private List<Long> consenterIds;
  private Map<String, Integer> drops;
  private Map<String, Integer> adds;
  private Type type;
  private Status status;
  private List<WaiverBudget> waiverBudget;
  private Settings settings;

  /**
   * Get the adds map, returning an empty map if null.
   *
   * @return map of player IDs to roster IDs for added players
   */
  public Map<String, Integer> getAdds() {
    return adds != null ? adds : new HashMap<>();
  }

  /** Transaction type. */
  public enum Type {
    TRADE,
    WAIVER,
    COMMISSIONER,
    FREE_AGENT
  }

  /** Transaction status. */
  public enum Status {
    COMPLETE,
    FAILED
  }

  /** Waiver budget information for transactions involving FAAB. */
  @Data
  @Builder
  public static class WaiverBudget {
    private int amount;
    private int receiver;
    private int sender;
  }

  /** Transaction settings. */
  @Data
  @Builder
  public static class Settings {
    private int waiverBid;
  }
}
