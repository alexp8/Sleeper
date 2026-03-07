package model.report;

import lombok.Builder;
import lombok.Data;

/** Waiver statistics for a single user. */
@Data
@Builder
public class UserWaiverStats {
  private String userName;
  private String userId;
  private int rosterId;
  private int totalWaivers;
  private long waiveredForRbs;
  private long waiveredForQbs;
  private long waiveredForWrs;
  private long waiveredForTes;
  private int totalFabSpent;
}
