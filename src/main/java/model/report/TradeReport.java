package model.report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** Top-level trade report containing all trade statistics. */
@Data
@Builder
public class TradeReport {
  private LocalDateTime generatedAt;
  private int totalTrades;
  private List<UserTradeStats> userTradeStats;
  private Map<String, Long> mostTradedPlayers;
  private Map<String, Long> mostDroppedPlayers;
  private Map<String, Long> mostTradedRbs;
  private Map<String, Long> mostTradedQbs;
  private List<BeansStats> beansStats;
}
