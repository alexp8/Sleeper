package calculation;

import model.Player;
import model.Roster;
import model.Transaction;
import model.User;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class CalcTrades extends Calculation {

    public static void calcTrades(List<Roster> rosters, Map<String, Player> nflPlayers, List<Transaction> transactions, List<User> users) {

        logger.info("Beginning calcTrades");

        // filter each player on its position
        List<String> runningBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.RB);
        List<String> quarterBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.QB);
        List<String> wideReceiverIds = Player.getPlayers(nflPlayers.values(), Player.Position.WR);
        List<String> tightEndIds = Player.getPlayers(nflPlayers.values(), Player.Position.TE);

        // crunch data
        for (User user : users) {

            // find this user's roster
            Roster roster = rosters.stream()
                    .filter(r -> r.getOwnerId().equalsIgnoreCase(user.getUserId()))
                    .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find roster for user: " + user));

            // find trades involved with this player
            List<Transaction> userTrades = transactions.stream()
                    .filter(x -> x.getStatus() == Transaction.Status.COMPLETE) // completed trades
                    .filter(x -> x.getType() == Transaction.Type.TRADE) // trade transactions
                    .filter(x -> x.getConsenterIds().stream().anyMatch(id -> id == roster.getRosterId())) // trades with this roster id
                    .filter(x -> x.getWaiverBudget().isEmpty() || x.getWaiverBudget().stream().allMatch(a -> a.getAmount() > 1)) // filter out bean trades
                    .collect(Collectors.toList());
            logger.info("{} has {} trades", user.getName(), userTrades.size());

            // traded for rbs
            List<Transaction> tradedForRbs = userTrades.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> runningBackIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} traded for {} rbs", user.getName(), tradedForRbs.size());

            // traded for wrs
            List<Transaction> tradedForWrs = userTrades.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> wideReceiverIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} traded for {} wrs", user.getName(), tradedForWrs.size());

            // traded for tes
            List<Transaction> tradedForTes = userTrades.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
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
    }


}
