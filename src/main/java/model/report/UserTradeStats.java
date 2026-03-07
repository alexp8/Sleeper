package model.report;

import lombok.Builder;
import lombok.Data;

/** Trade statistics for a single user. */
@Data
@Builder
public class UserTradeStats {
  private String userName;
  private String userId;
  private int rosterId;
  private int totalTrades;
  private long tradedForRbs;
  private long tradedForQbs;
  private long tradedForWrs;
  private long tradedForTes;
}
