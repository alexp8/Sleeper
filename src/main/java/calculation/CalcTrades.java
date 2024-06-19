package calculation;

import model.Player;
import model.Roster;
import model.Transaction;
import model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalcTrades extends Calculation {

    public static void calcTrades(List<Roster> rosters, Map<String, Player> nflPlayers, List<Transaction> transactions, List<User> users) {

        logger.info("Beginning calcTrades");

        // filter each player on its position
        List<String> runningBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.RB);
        List<String> quarterBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.QB);
        List<String> wideReceiverIds = Player.getPlayers(nflPlayers.values(), Player.Position.WR);
        List<String> tightEndIds = Player.getPlayers(nflPlayers.values(), Player.Position.TE);

        // completed trades
        List<Transaction> trades = transactions.stream()
                .filter(t -> t.getStatus() == Transaction.Status.COMPLETE)
                .filter(t -> t.getType() == Transaction.Type.TRADE)
                .collect(Collectors.toList());

        // crunch data
        for (User user : users) {

            // find this user's roster
            Roster roster = Roster.getUserRoster(rosters, user.getUserId());

            // find trades involved with this player
            List<Transaction> userTrades = trades.stream()
                    .filter(t -> t.getConsenterIds().contains((long) roster.getRosterId())) // trades with this roster id
                    .filter(t -> t.getWaiverBudget().isEmpty() || t.getWaiverBudget().stream().allMatch(a -> a.getAmount() > 1)) // filter out bean trades
                    .collect(Collectors.toList());
            logger.info("{} has {} trades", user.getName(), userTrades.size());

            // traded for rbs
          int tradedForRbs = (int) userTrades.stream()
                    .filter(t -> t.getAdds().entrySet().stream()
                            .anyMatch(add -> runningBackIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .count();
            logger.info("{} traded for {} rbs", user.getName(), tradedForRbs);

            // traded for wrs
            List<Transaction> tradedForWrs = userTrades.stream()
                    .filter(t -> t.getAdds().entrySet().stream()
                            .anyMatch(add -> wideReceiverIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} traded for {} wrs", user.getName(), tradedForWrs.size());

            // traded for tes
            List<Transaction> tradedForTes = userTrades.stream()
                    .filter(t -> t.getAdds().entrySet().stream()
                            .anyMatch(add -> tightEndIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} traded for {} tes", user.getName(), tradedForTes.size());

            // traded for qbs
            List<Transaction> tradedForQbs = userTrades.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> quarterBackIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} traded for {} qbs", user.getName(), tradedForQbs.size());

            logger.info("");
        }

        // calculate most traded player
        mostTradedPlayer(nflPlayers, trades);

        // calculate most dropped player
        mostDroppedPlayers(nflPlayers, transactions);

        // calculate most traded RB
        mostTradedRbplayers(nflPlayers, trades);

        // calculate most traded QB
        mostTradedQbplayers(nflPlayers, trades);

        // most beans spent in trades
        mostBeansSpent(trades, users, rosters);

    }

    private static void mostBeansSpent(List<Transaction> trades, List<User> users, List<Roster> rosters) {

        for (User user : users) {

            // find this user's roster
            Roster roster = Roster.getUserRoster(rosters, user.getUserId());

            int beansReceivedFromTrades = trades.stream()
                    .filter(x -> x.getRosterIds().contains((long) roster.getRosterId()))
                    .filter(t -> t.getWaiverBudget() != null && t.getWaiverBudget().size() > 0)
                    .flatMap(t -> t.getWaiverBudget().stream())
                    .filter(w -> w.getReceiver() == roster.getRosterId()) // received beans
                    .mapToInt(Transaction.WaiverBudget::getAmount)
                    .sum();

            logger.info("{} has received {} beans in trades", user.getName(), beansReceivedFromTrades);

            int beansSpentInTrades = trades.stream()
                    .filter(x -> x.getRosterIds().contains((long) roster.getRosterId()))
                    .filter(t -> t.getWaiverBudget() != null && t.getWaiverBudget().size() > 0)
                    .flatMap(t -> t.getWaiverBudget().stream())
                    .filter(w -> w.getSender() == roster.getRosterId()) // received beans
                    .mapToInt(Transaction.WaiverBudget::getAmount)
                    .sum();

            logger.info("{} has spent {} beans in trades", user.getName(), beansSpentInTrades);
        }
        logger.info("");
    }

    private static void mostTradedPlayer(Map<String, Player> nflPlayers, List<Transaction> trades) {
        Map<String, Long> tradesCountMap = trades.stream()
                .flatMap(transaction -> transaction.getAdds().keySet().stream())
                .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

        Map<String, Long> mostTradedNflPlayers = nflPlayers.values().stream()
                .filter(p -> !p.getPlayerId().equalsIgnoreCase("0"))
                .filter(p -> trades.stream().anyMatch(t -> t.getAdds().containsKey(p.getPlayerId())))
                .collect(Collectors.toMap(
                        Player::getName,
                        player -> tradesCountMap.getOrDefault(player.getPlayerId(), 0L)
                )).entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // Sort by value descending
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // In case of key collisions, which won't happen here
                        LinkedHashMap::new // Preserve order
                ));
        logger.info("Most traded nfl players {}\n", mostTradedNflPlayers.toString());
    }

    private static void mostDroppedPlayers(Map<String, Player> nflPlayers, List<Transaction> transactions) {
        List<Transaction> drops = transactions.stream()
                .filter(drop -> drop.getDrops() != null)
                .filter(x -> x.getStatus() == Transaction.Status.COMPLETE)
                .filter(x -> x.getType() == Transaction.Type.FREE_AGENT || x.getType() == Transaction.Type.WAIVER)
                .collect(Collectors.toList());

        Map<String, Long> dropsCountMap = drops.stream()
                .flatMap(drop -> drop.getDrops().keySet().stream())
                .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

        Map<String, Long> mostDroppedNflPlayers = nflPlayers.values().stream()
                .filter(p -> !p.getPlayerId().equalsIgnoreCase("0"))
                .filter(p -> drops.stream().anyMatch(t -> t.getDrops().containsKey(p.getPlayerId())))
                .collect(Collectors.toMap(
                        Player::getName,
                        player -> dropsCountMap.getOrDefault(player.getPlayerId(), 0L)
                )).entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // Sort by value descending
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // In case of key collisions, which won't happen here
                        LinkedHashMap::new // Preserve order
                ));
        logger.info("Most dropped nfl players {}\n", mostDroppedNflPlayers.toString());
    }

    private static void mostTradedQbplayers(Map<String, Player> nflPlayers, List<Transaction> trades) {

        Map<String, Long> playerTradeMap = trades.stream()
                .flatMap(trade -> trade.getAdds().keySet().stream())
                .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

        Map<String, Long> mostTradedRbPlayers = nflPlayers.values().stream()
                .filter(p -> !p.getPlayerId().equalsIgnoreCase("0"))
                .filter(p -> p.getPosition() == Player.Position.QB)
                .filter(p -> trades.stream().anyMatch(t -> t.getAdds().containsKey(p.getPlayerId())))
                .collect(Collectors.toMap(
                        Player::getName,
                        player -> playerTradeMap.getOrDefault(player.getPlayerId(), 0L)
                )).entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // Sort by value descending
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // In case of key collisions, which won't happen here
                        LinkedHashMap::new // Preserve order
                ));
        logger.info("Most traded QBs {}\n", mostTradedRbPlayers.toString());
    }

    private static void mostTradedRbplayers(Map<String, Player> nflPlayers, List<Transaction> trades) {

        Map<String, Long> playerTradeMap = trades.stream()
                .flatMap(trade -> trade.getAdds().keySet().stream())
                .collect(Collectors.groupingBy(playerId -> playerId, Collectors.counting()));

        Map<String, Long> mostTradedRbPlayers = nflPlayers.values().stream()
                .filter(p -> !p.getPlayerId().equalsIgnoreCase("0"))
                .filter(p -> p.getPosition() == Player.Position.RB)
                .filter(p -> trades.stream().anyMatch(t -> t.getAdds().containsKey(p.getPlayerId())))
                .collect(Collectors.toMap(
                        Player::getName,
                        player -> playerTradeMap.getOrDefault(player.getPlayerId(), 0L)
                )).entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // Sort by value descending
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // In case of key collisions, which won't happen here
                        LinkedHashMap::new // Preserve order
                ));
        logger.info("Most traded RBs {}\n", mostTradedRbPlayers.toString());
    }

}
