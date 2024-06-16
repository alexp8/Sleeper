package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.*;

import java.util.List;
import java.util.Map;

public class SleeperRest {

    public static String getNflPlayersJson() {

        String url = "https://api.sleeper.app/v1/players/nfl";
        Response response = getResponse(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching players: " + response.getBody());
        }

        return response.getBody().asString();
    }

    public static Map<String, Player> getNflPlayers() {

        String url = "https://api.sleeper.app/v1/players/nfl";
        Response response = getResponse(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching players: " + response.getBody());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response.getBody().asString(), objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Player.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Transaction> getTransactions(String leagueId, String round) {

        String url = String.format("https://api.sleeper.app/v1/league/%s/transactions/%s", leagueId, round);
        Response response = getResponse(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching transactions: " + response.getBody());
        }

        return response.jsonPath().getList("", Transaction.class);
    }

    public static List<User> getUsers(String leagueId) {

        String url = String.format("https://api.sleeper.app/v1/league/%s/users", leagueId);
        Response response = getResponse(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching users: " + response.getBody());
        }

        return response.jsonPath().getList("", User.class);
    }

    public static List<Roster> getRosters(String leagueId) {

        String url = String.format("https://api.sleeper.app/v1/league/%s/rosters", leagueId);
        Response response = RestAssured.get(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching rosters: " + response.getBody());
        }

        return response.jsonPath().getList("", Roster.class);
    }

    public static List<Matchup> getMatchups(String leagueId, String week) {
        String url = String.format("https://api.sleeper.app/v1/league/%s/matchups/%s", leagueId, week);
        Response response = getResponse(url);

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed fetching matchups: " + response.getBody());
        }

        return response.jsonPath().getList("", Matchup.class);
    }

    private static Response getResponse(String url) {
        return RestAssured.given()
                .log().uri()
                .get(url);
    }
}
