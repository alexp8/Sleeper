package runner;

import calculation.CalcMatchups;
import calculation.CalcTrades;
import calculation.CalcWaivers;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.SleeperRest;
import util.FileHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    private static final String CAVE_SLEEPER_ID_2022 = "869324695290400768";
    private static final String CAVE_SLEEPER_ID_2023 = "916422844907630592";
    private static final String CAVE_SLEEPER_ID_2024 = "1071255073365331968";

    public static void main(String... args) {

        // query Sleeper for data
        Map<String, Player> nflPlayers = FileHelper.getNflPlayers();
        List<Roster> rosters = getAllRosters();
        List<Matchup> matchups = getAllMatchups();
        List<Transaction> transactions = getAllTransactions();

        logger.info("{} rosters", rosters.size());
        logger.info("{} nfl players", nflPlayers.size());
        logger.info("{} matchups", matchups.size());
        logger.info("{} total transactions", transactions.size());

        // get all users
        List<User> users = SleeperRest.getUsers(CAVE_SLEEPER_ID_2024);
        logger.info("users {}", users);

        CalcTrades.calcTrades(rosters, nflPlayers, transactions, users);

        CalcWaivers.calcWaivers(rosters, nflPlayers, transactions, users);

        CalcMatchups.calcMatchups(rosters, nflPlayers, matchups, users);

    }

    private static List<Matchup> getAllMatchups() {
        List<Matchup> matchups = new ArrayList<>();
        for (int round = 0; round < 20; round++) {
            try {
                List<Matchup> newMatchups = List.of(
                        SleeperRest.getMatchups(CAVE_SLEEPER_ID_2022, String.format("%s", round)),
                        SleeperRest.getMatchups(CAVE_SLEEPER_ID_2023, String.format("%s", round)),
                        SleeperRest.getMatchups(CAVE_SLEEPER_ID_2024, String.format("%s", round))
                ).stream().flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());

                matchups.addAll(newMatchups);
            } catch (Exception ignored) {

            }
        }

        return matchups;
    }


    private static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (int round = 0; round < 20; round++) {
            try {
                List<Transaction> newTransactions = List.of(
                        SleeperRest.getTransactions(CAVE_SLEEPER_ID_2022, String.format("%s", round)),
                        SleeperRest.getTransactions(CAVE_SLEEPER_ID_2023, String.format("%s", round)),
                        SleeperRest.getTransactions(CAVE_SLEEPER_ID_2024, String.format("%s", round))
                ).stream().flatMap(Collection::stream)
                        .distinct()
                        .collect(Collectors.toList());

                transactions.addAll(newTransactions);
            } catch (Exception ignored) {

            }
        }

        return transactions;
    }

    private static List<Roster> getAllRosters() {
        return List.of(
                SleeperRest.getRosters(CAVE_SLEEPER_ID_2022),
                SleeperRest.getRosters(CAVE_SLEEPER_ID_2023),
                SleeperRest.getRosters(CAVE_SLEEPER_ID_2024)
        ).stream().flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

}
