package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.SleeperApiException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import model.dto.*;
import util.PropertiesUtil;

/**
 * REST client for interacting with the Sleeper Fantasy Football API. Returns DTOs that directly
 * represent the API response structure. For domain models, use DataHelper which maps DTOs to domain
 * objects. Uses HttpService for HTTP communication.
 */
@Slf4j
public class SleeperRest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String BASE_URL = PropertiesUtil.getInstance().getSleeperApiBaseUrl();

  /**
   * Fetches all NFL players as a JSON string.
   *
   * @return JSON string containing all NFL players
   * @throws SleeperApiException if the API request fails
   */
  public static String getNflPlayersJson() {
    String url = BASE_URL + "/players/nfl";
    String responseBody = HttpService.get(url, "Failed fetching players");
    return formatJson(responseBody);
  }

  /**
   * Fetches all NFL players as a map of player ID to Player object.
   *
   * @return Map of player IDs to Player objects
   * @throws SleeperApiException if the API request or parsing fails
   */
  public static Map<String, PlayerResponse.PlayerDto> getNflPlayers() {
    String url = BASE_URL + "/players/nfl";
    String responseBody = HttpService.get(url, "Failed fetching players");

    try {
      return OBJECT_MAPPER.readValue(
          responseBody,
          OBJECT_MAPPER
              .getTypeFactory()
              .constructMapType(Map.class, String.class, PlayerResponse.PlayerDto.class));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse NFL players response", e);
      throw new SleeperApiException("Failed to parse NFL players response", e);
    }
  }

  /**
   * Fetches transactions for a specific league and round.
   *
   * @param leagueId the league identifier
   * @param round the round/week number
   * @return List of Transaction objects
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if parameters are null or empty
   */
  public static List<TransactionDto> getTransactions(String leagueId, String round) {
    validateParameter(leagueId, "leagueId");
    validateParameter(round, "round");

    String url = String.format("%s/league/%s/transactions/%s", BASE_URL, leagueId, round);
    String responseBody = HttpService.get(url, "Failed fetching transactions");

    return parseList(responseBody, TransactionDto.class, "output/transactions");
  }

  /**
   * Fetches transactions for a specific league and round as a JSON string.
   *
   * @param leagueId the league identifier
   * @param round the round/week number
   * @return JSON string containing transactions
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if parameters are null or empty
   */
  public static String getTransactionsJson(String leagueId, String round) {
    validateParameter(leagueId, "leagueId");
    validateParameter(round, "round");

    String url = String.format("%s/league/%s/transactions/%s", BASE_URL, leagueId, round);
    String responseBody = HttpService.get(url, "Failed fetching transactions");

    return formatJson(responseBody);
  }

  /**
   * Fetches league information for a specific league.
   *
   * @param leagueId the league identifier
   * @return LeagueDto object containing league information
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if leagueId is null or empty
   */
  public static LeagueDto getLeague(String leagueId) {
    validateParameter(leagueId, "leagueId");

    String url = String.format("%s/league/%s", BASE_URL, leagueId);
    String responseBody = HttpService.get(url, "Failed fetching league");

    try {
      return OBJECT_MAPPER.readValue(responseBody, LeagueDto.class);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse league response", e);
      throw new SleeperApiException("Failed to parse league response", e);
    }
  }

  /**
   * Fetches all users in a specific league.
   *
   * @param leagueId the league identifier
   * @return List of User objects
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if leagueId is null or empty
   */
  public static List<UserDto> getUsers(String leagueId) {
    validateParameter(leagueId, "leagueId");

    String url = String.format("%s/league/%s/users", BASE_URL, leagueId);
    String responseBody = HttpService.get(url, "Failed fetching users");

    return parseList(responseBody, UserDto.class, "users");
  }

  /**
   * Fetches all rosters in a specific league.
   *
   * @param leagueId the league identifier
   * @return List of Roster objects
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if leagueId is null or empty
   */
  public static List<RosterDto> getRosters(String leagueId) {
    validateParameter(leagueId, "leagueId");

    String url = String.format("%s/league/%s/rosters", BASE_URL, leagueId);
    String responseBody = HttpService.get(url, "Failed fetching rosters");

    return parseList(responseBody, RosterDto.class, "rosters");
  }

  /**
   * Fetches matchups for a specific league and week.
   *
   * @param leagueId the league identifier
   * @param week the week number
   * @return List of Matchup objects
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if parameters are null or empty
   */
  public static List<MatchupDto> getMatchups(String leagueId, String week) {
    validateParameter(leagueId, "leagueId");
    validateParameter(week, "week");

    String url = String.format("%s/league/%s/matchups/%s", BASE_URL, leagueId, week);
    String responseBody = HttpService.get(url, "Failed fetching matchups");

    return parseList(responseBody, MatchupDto.class, "output/matchups");
  }

  /**
   * Fetches matchups for a specific league and week as a JSON string.
   *
   * @param leagueId the league identifier
   * @param week the week number
   * @return JSON string containing matchups
   * @throws SleeperApiException if the API request fails
   * @throws IllegalArgumentException if parameters are null or empty
   */
  public static String getMatchupsContent(String leagueId, String week) {
    validateParameter(leagueId, "leagueId");
    validateParameter(week, "week");

    String url = String.format("%s/league/%s/matchups/%s", BASE_URL, leagueId, week);
    String responseBody = HttpService.get(url, "Failed fetching matchups");

    return formatJson(responseBody);
  }

  /**
   * Parses a JSON array string into a List of objects.
   *
   * @param json the JSON array string
   * @param clazz the class type to parse into
   * @param typeName the type name for error messages
   * @param <T> the type parameter
   * @return List of parsed objects
   * @throws SleeperApiException if parsing fails
   */
  private static <T> List<T> parseList(String json, Class<T> clazz, String typeName) {
    try {
      return OBJECT_MAPPER.readValue(
          json, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    } catch (JsonProcessingException e) {
      log.error("Failed to parse {} response", typeName, e);
      throw new SleeperApiException("Failed to parse " + typeName + " response", e);
    }
  }

  /**
   * Formats JSON string with pretty printing.
   *
   * @param json the JSON string to format
   * @return formatted JSON string
   * @throws SleeperApiException if formatting fails
   */
  private static String formatJson(String json) {
    try {
      Object jsonObject = OBJECT_MAPPER.readValue(json, Object.class);
      return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    } catch (JsonProcessingException e) {
      log.warn("Failed to format JSON, returning original", e);
      return json;
    }
  }

  /**
   * Validates that a parameter is not null or empty.
   *
   * @param parameter the parameter to validate
   * @param parameterName the name of the parameter for error messages
   * @throws IllegalArgumentException if the parameter is null or empty
   */
  private static void validateParameter(String parameter, String parameterName) {
    if (parameter == null || parameter.trim().isEmpty()) {
      String message = String.format("Parameter '%s' cannot be null or empty", parameterName);
      log.error(message);
      throw new IllegalArgumentException(message);
    }
  }
}
