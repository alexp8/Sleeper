package runner;

import calculation.CalcMatchups;
import calculation.CalcTrades;
import calculation.CalcWaivers;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.SleeperRest;
import util.DataHelper;
import util.FileHelper;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    private static final String CAVE_SLEEPER_ID_2022 = "869324695290400768";
    private static final String CAVE_SLEEPER_ID_2023 = "916422844907630592";
    private static final String CAVE_SLEEPER_ID_2024 = "1071255073365331968";
    private static final List<String> LEAGUE_IDS = List.of(CAVE_SLEEPER_ID_2022, CAVE_SLEEPER_ID_2023, CAVE_SLEEPER_ID_2024);

    /**
     * The first time this code is run, will query data from sleeper and store the data in src/main/resources/
     */
    public static void main(String... args) {

        // query Sleeper for data
        Map<String, Player> nflPlayers = DataHelper.getNflPlayers(false);
        List<Roster> rosters = getAllRosters();
        List<Matchup> matchups = DataHelper.getMatchups(LEAGUE_IDS, false);
        List<Transaction> transactions = DataHelper.getTransactions(LEAGUE_IDS, false);

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

        // players whose first and last name start with the same letter
        List<String> playersName = nflPlayers.values().stream()
                .filter(p -> matchups.stream()
                        .anyMatch(m -> m.getPlayerPoints().containsKey(p.getPlayerId())))
                .filter(p -> p.getFirstName().charAt(0) == p.getLastName().charAt(0))
                .sorted(Comparator.comparing(Player::getName))
                .map(Player::getName)
                .collect(Collectors.toList());
        logger.info("Players whose name starts with same letter {}", playersName);
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
