package calculation;

import model.Player;
import model.Roster;
import model.Transaction;
import model.User;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class CalcWaivers extends Calculation {

    public static void calcWaivers(List<Roster> rosters, Map<String, Player> nflPlayers, List<Transaction> transactions, List<User> users) {

        logger.info("Beginning calcWaivers");

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
            List<Transaction> userWaivers = transactions.stream()
                    .filter(x -> x.getStatus() == Transaction.Status.COMPLETE) // completed trades
                    .filter(x -> x.getType() == Transaction.Type.WAIVER) // trade transactions
                    .filter(x -> x.getConsenterIds().stream().anyMatch(id -> id == roster.getRosterId())) // trades with this roster id
                    .filter(x -> x.getWaiverBudget().isEmpty() || x.getWaiverBudget().stream().allMatch(a -> a.getAmount() > 1)) // filter out bean trades
                    .collect(Collectors.toList());
            logger.info("{} has {} waivers", user.getName(), userWaivers.size());

            // traded for rbs
            List<Transaction> tradedForRbs = userWaivers.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> runningBackIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} waivered for {} rbs", user.getName(), tradedForRbs.size());

            // traded for wrs
            List<Transaction> tradedForWrs = userWaivers.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> wideReceiverIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} waivered for {} wrs", user.getName(), tradedForWrs.size());

            // traded for tes
            List<Transaction> tradedForTes = userWaivers.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> tightEndIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} waivered for {} tes", user.getName(), tradedForTes.size());

            // traded for qbs
            List<Transaction> tradedForQbs = userWaivers.stream()
                    .filter(x -> x.getAdds().entrySet().stream()
                            .anyMatch(add -> quarterBackIds.contains(add.getKey()) && roster.getRosterId() == add.getValue()))
                    .collect(Collectors.toList());
            logger.info("{} waivered for {} qbs", user.getName(), tradedForQbs.size());

            int totalFabSpent = userWaivers.stream()
                    .filter(waiver -> waiver.getSettings() != null)
                    .mapToInt(waiver -> waiver.getSettings().getWaiverBid())
                    .sum();
            logger.info("{} has spent a total of ${} on waivers", user.getName(), totalFabSpent);

            logger.info("");
        }

    }


}
