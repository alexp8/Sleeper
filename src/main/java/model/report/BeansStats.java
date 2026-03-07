package model.report;

import lombok.Builder;
import lombok.Data;

/** Bean spending statistics for a single user. */
@Data
@Builder
public class BeansStats {
  private String userName;
  private String userId;
  private int rosterId;
  private int beansReceived;
  private int beansSpent;
  private int netBeans; // received - spent
}
