package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Matchup;
import model.Player;
import model.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.SleeperRest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataHelper {
    protected static final Logger logger = LogManager.getLogger();

    public static final Path SRC_MAIN_RESOURCES = Paths.get("Sleeper", "src", "main", "resources");

    public static Map<String, Player> getNflPlayers(boolean freshNew) {

        // read data from local
        Path pathToNflPlayersJson = Paths.get(SRC_MAIN_RESOURCES.toString(), "nfl_players.json");
        String nflPlayersContent = FileHelper.getFileContent(pathToNflPlayersJson);

        if (nflPlayersContent.isEmpty() || freshNew) {
            nflPlayersContent = SleeperRest.getNflPlayersJson();
            FileHelper.write(pathToNflPlayersJson, nflPlayersContent); // save nfl players data to local
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(nflPlayersContent, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Player.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static List<Matchup> getMatchups(List<String> leagueIds, boolean freshNew) {

        List<Matchup> matchups = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String leagueId : leagueIds) {
            for (int round = 0; round < 20; round++) {
                try {

                    // read data from local
                    Path pathToNflPlayersJson = Paths.get(SRC_MAIN_RESOURCES.toString(), String.format("matchups/matchup_%s_%s.json", leagueId, round));
                    String matchupContent = FileHelper.getFileContent(pathToNflPlayersJson);

                    // if local doesn't have data, query Sleeper for the data then save it
                    if (matchupContent.isEmpty() || freshNew) {
                        matchupContent = SleeperRest.getMatchupsContent(leagueId, String.format("%s", round));
                        FileHelper.write(pathToNflPlayersJson, matchupContent); // save nfl players data to local
                    }

                    // parse the json
                    List<Matchup> newMatchups = objectMapper.readValue(matchupContent, objectMapper.getTypeFactory().constructCollectionType(List.class, Matchup.class));

                    if (!newMatchups.isEmpty())
                        matchups.addAll(newMatchups);
                } catch (Exception ignored) {

                }
            }
        }

        return matchups.stream().distinct().collect(Collectors.toList());
    }

    public static List<Transaction> getTransactions(List<String> leagueIds, boolean freshNew) {
        List<Transaction> transactions = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String leagueId : leagueIds) {
            for (int round = 0; round < 20; round++) {
                try {

                    // read data from local
                    Path transactionsJson = Paths.get(SRC_MAIN_RESOURCES.toString(), String.format("transactions/transaction_%s_%s.json", leagueId, round));
                    String transactionContent = FileHelper.getFileContent(transactionsJson);

                    // if local doesn't have data, query Sleeper for the data then save it
                    if (transactionContent.isEmpty() || freshNew) {
                        transactionContent = SleeperRest.getTransactionsJson(leagueId, String.format("%s", round));
                        FileHelper.write(transactionsJson, transactionContent); // save nfl players data to local
                    }

                    // parse the json
                    List<Transaction> newTransactions = objectMapper.readValue(transactionContent, objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class));

                    if (!newTransactions.isEmpty())
                        transactions.addAll(newTransactions);
                } catch (Exception ignored) {

                }
            }
        }

        return transactions.stream().distinct().collect(Collectors.toList());
    }
}
