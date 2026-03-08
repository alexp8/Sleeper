package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.SleeperApiException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.dto.MatchupDto;
import model.dto.PlayerResponse;
import model.dto.TransactionDto;
import model.mapper.DomainMapper;
import service.SleeperRest;

/**
 * Helper class for fetching and caching Sleeper API data locally. Fetches DTOs from the API/cache
 * and maps them to domain models. Provides methods to retrieve NFL players, matchups, and
 * transactions with automatic caching to reduce API calls.
 */
@Slf4j
public class DataHelper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final int MAX_ROUNDS = 20;
  private static final Path DATA_DIRECTORY = Paths.get("sleeper_data");

  /**
   * Fetches all NFL players from cache or API and maps them to domain models.
   *
   * @param forceRefresh if true, fetches fresh data from API; if false, uses cached data if
   *     available
   * @return Map of player IDs to Player domain model objects
   * @throws SleeperApiException if API request or JSON parsing fails
   * @throws IllegalStateException if data cannot be retrieved or parsed
   */
  public static Map<String, Player> getNflPlayers(boolean forceRefresh) {
    log.debug("Fetching NFL players (forceRefresh: {})", forceRefresh);

    Path filePath = DATA_DIRECTORY.resolve("nfl_players.json");
    String content = FileHelper.getFileContent(filePath);

    if (content.isEmpty() || forceRefresh) {
      log.info("Fetching fresh NFL players data from API");
      content = SleeperRest.getNflPlayersJson();
      FileHelper.write(filePath, content);
    } else {
      log.debug("Using cached NFL players data");
    }

    try {
      // Parse to DTOs first
      Map<String, PlayerResponse.PlayerDto> dtoMap =
          OBJECT_MAPPER.readValue(
              content,
              OBJECT_MAPPER
                  .getTypeFactory()
                  .constructMapType(Map.class, String.class, PlayerResponse.PlayerDto.class));

      // Map DTOs to domain models
      return DomainMapper.toPlayerMap(dtoMap);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse NFL players JSON", e);
      throw new SleeperApiException("Failed to parse NFL players data", e);
    }
  }

  /**
   * Fetches matchups for multiple leagues across all rounds and maps to domain models.
   *
   * @param leagueIds list of league identifiers to fetch matchups for
   * @param forceRefresh if true, fetches fresh data from API; if false, uses cached data if
   *     available
   * @return List of distinct Matchup domain model objects across all leagues and rounds
   * @throws IllegalArgumentException if leagueIds is null or empty
   * @throws SleeperApiException if API requests fail
   */
  public static List<Matchup> getMatchups(List<String> leagueIds, boolean forceRefresh) {
    validateLeagueIds(leagueIds);
    log.debug(
        "Fetching matchups for {} leagues (forceRefresh: {})", leagueIds.size(), forceRefresh);

    // Fetch DTOs
    List<MatchupDto> dtos =
        fetchDataForLeaguesAndRounds(
            leagueIds,
            forceRefresh,
            "matchups/matchup_%s_%s.json",
            (leagueId, round) -> SleeperRest.getMatchupsContent(leagueId, String.valueOf(round)),
            MatchupDto.class);

    // Map to domain models
    return DomainMapper.toMatchupList(dtos);
  }

  /**
   * Fetches transactions for multiple leagues across all rounds and maps to domain models.
   *
   * @param leagueIds list of league identifiers to fetch transactions for
   * @param forceRefresh if true, fetches fresh data from API; if false, uses cached data if
   *     available
   * @return List of distinct Transaction domain model objects across all leagues and rounds
   * @throws IllegalArgumentException if leagueIds is null or empty
   * @throws SleeperApiException if API requests fail
   */
  public static List<Transaction> getTransactions(List<String> leagueIds, boolean forceRefresh) {
    validateLeagueIds(leagueIds);
    log.debug(
        "Fetching transactions for {} leagues (forceRefresh: {})", leagueIds.size(), forceRefresh);

    // Fetch DTOs
    List<TransactionDto> dtos =
        fetchDataForLeaguesAndRounds(
            leagueIds,
            forceRefresh,
            "transactions/transaction_%s_%s.json",
            (leagueId, round) -> SleeperRest.getTransactionsJson(leagueId, String.valueOf(round)),
            TransactionDto.class);

    // Map to domain models
    return DomainMapper.toTransactionList(dtos);
  }

  /**
   * Generic method to fetch DTO data for multiple leagues and rounds with caching.
   *
   * @param leagueIds list of league identifiers
   * @param forceRefresh whether to force refresh from API
   * @param filePathPattern pattern for cache file path (e.g., "matchups/matchup_%s_%s.json")
   * @param apiCall function to fetch data from API (takes leagueId and round)
   * @param clazz DTO class type to parse JSON into
   * @param <T> DTO type parameter for the return list
   * @return List of distinct DTO objects fetched across all leagues and rounds
   */
  private static <T> List<T> fetchDataForLeaguesAndRounds(
      List<String> leagueIds,
      boolean forceRefresh,
      String filePathPattern,
      BiFunction<String, Integer, String> apiCall,
      Class<T> clazz) {

    List<T> results = new ArrayList<>();

    for (String leagueId : leagueIds) {
      for (int round = 0; round < MAX_ROUNDS; round++) {
        try {
          Path filePath = DATA_DIRECTORY.resolve(String.format(filePathPattern, leagueId, round));
          String content = FileHelper.getFileContent(filePath);

          if (content.isEmpty() || forceRefresh) {
            log.debug("Fetching fresh data from API: league={}, round={}", leagueId, round);
            content = apiCall.apply(leagueId, round);
            FileHelper.write(filePath, content);
          }

          List<T> parsedItems =
              OBJECT_MAPPER.readValue(
                  content,
                  OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));

          if (!parsedItems.isEmpty()) {
            results.addAll(parsedItems);
          }

        } catch (SleeperApiException e) {
          log.warn(
              "API error fetching data for league={}, round={}: {}",
              leagueId,
              round,
              e.getMessage());
          // Continue with other rounds/leagues
        } catch (JsonProcessingException e) {
          log.error(
              "JSON parsing error for league={}, round={}: {}", leagueId, round, e.getMessage());
          // Continue with other rounds/leagues
        } catch (Exception e) {
          log.error(
              "Unexpected error fetching data for league={}, round={}: {}",
              leagueId,
              round,
              e.getMessage(),
              e);
          // Continue with other rounds/leagues
        }
      }
    }

    return results.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Validates that the leagueIds parameter is not null or empty.
   *
   * @param leagueIds list of league identifiers to validate
   * @throws IllegalArgumentException if leagueIds is null or empty
   */
  private static void validateLeagueIds(List<String> leagueIds) {
    if (leagueIds == null || leagueIds.isEmpty()) {
      String message = "Parameter 'leagueIds' cannot be null or empty";
      log.error(message);
      throw new IllegalArgumentException(message);
    }
  }
}
