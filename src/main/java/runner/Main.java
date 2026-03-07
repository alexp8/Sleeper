package runner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import model.Matchup;
import model.Player;
import model.Roster;
import model.Transaction;
import model.User;
import model.mapper.DomainMapper;
import service.ReportService;
import service.SleeperRest;
import util.DataHelper;
import util.PropertiesUtil;

@Slf4j
public class Main {

  /**
   * Main entry point for the Sleeper Fantasy Football Analytics application.
   *
   * <p>Supports command-line arguments for controlling data refresh, year selection, and analysis
   * types. Run with --help to see all available options.
   *
   * @param args command-line arguments
   */
  public static void main(String... args) {

    // Parse command-line arguments
    CommandLineOptions options = parseCommandLineOptions(args);

    // Display help if requested
    if (options.isHelpRequested()) {
      System.out.println(CommandLineOptions.getHelpMessage());
      System.exit(0);
    }

    log.info("Starting Sleeper Analytics with options: {}", options);
    log.info("Selected years: {}", options.getYears());
    log.info("Force refresh: {}", options.isForceRefresh());

    // Get selected league IDs based on year selection
    List<String> selectedLeagueIds = options.getSelectedLeagueIds();
    if (selectedLeagueIds.isEmpty()) {
      log.error("No league IDs selected. Please specify valid years.");
      System.exit(1);
    }

    // Fetch common data needed for all analyses
    log.info("Fetching common data...");
    Map<String, Player> nflPlayers = DataHelper.getNflPlayers(options.isForceRefresh());
    List<Roster> rosters = getRosters(selectedLeagueIds);
    String latestLeagueId = getLatestLeagueId(options);
    model.League league = DomainMapper.toLeague(SleeperRest.getLeague(latestLeagueId));
    List<User> users =
        SleeperRest.getUsers(latestLeagueId).stream().map(DomainMapper::toUser).toList();

    // Validate data
    validateData(nflPlayers, rosters, users);

    log.info("{} NFL players loaded", nflPlayers.size());
    log.info("{} rosters loaded", rosters.size());
    log.info("{} users loaded", users.size());

    // Fetch analysis-specific data only when needed
    List<Transaction> transactions = null;
    List<Matchup> matchups = null;

    boolean needTransactions =
        options.shouldRunAnalysis(CommandLineOptions.AnalysisType.TRADES)
            || options.shouldRunAnalysis(CommandLineOptions.AnalysisType.WAIVERS);
    boolean needMatchups = options.shouldRunAnalysis(CommandLineOptions.AnalysisType.MATCHUPS);

    if (needTransactions) {
      log.info("Fetching transaction data...");
      transactions = DataHelper.getTransactions(selectedLeagueIds, options.isForceRefresh());
      log.info("{} transactions loaded", transactions.size());
    }

    if (needMatchups) {
      log.info("Fetching matchup data...");
      matchups = DataHelper.getMatchups(selectedLeagueIds, options.isForceRefresh());
      log.info("{} matchups loaded", matchups.size());
    }

    // Run selected analyses
    if (options.shouldRunAnalysis(CommandLineOptions.AnalysisType.TRADES)) {
      ReportService.generateTradeReport(rosters, nflPlayers, transactions, users);
    }

    if (options.shouldRunAnalysis(CommandLineOptions.AnalysisType.WAIVERS)) {
      ReportService.generateWaiverReport(rosters, nflPlayers, transactions, users);
    }

    if (options.shouldRunAnalysis(CommandLineOptions.AnalysisType.MATCHUPS)) {
      ReportService.generateMatchupReport(rosters, nflPlayers, matchups, users);
    }

    // Generate league overview report (index page)
    ReportService.generateLeagueReport(league, users, options.getYears().stream().toList());

    log.info("All analyses complete!");
  }

  /**
   * Validate that essential data has been loaded successfully.
   *
   * @param nflPlayers map of NFL players
   * @param rosters list of rosters
   * @param users list of users
   */
  private static void validateData(
      Map<String, Player> nflPlayers, List<Roster> rosters, List<User> users) {

    if (nflPlayers.isEmpty()) {
      log.warn("No NFL players loaded. Analysis results may be incomplete.");
    }

    if (rosters.isEmpty()) {
      log.error("No rosters found for selected leagues. Cannot proceed with analysis.");
      System.exit(1);
    }

    if (users.isEmpty()) {
      log.error("No users found. Cannot proceed with analysis.");
      System.exit(1);
    }
  }

  /**
   * Gets rosters for the specified league IDs.
   *
   * @param leagueIds list of league IDs to fetch rosters for
   * @return list of all rosters across the specified leagues
   */
  private static List<Roster> getRosters(List<String> leagueIds) {
    return leagueIds.stream()
        .map(SleeperRest::getRosters)
        .flatMap(Collection::stream)
        .distinct()
        .map(DomainMapper::toRoster)
        .toList();
  }

  /**
   * Parse command-line arguments and handle errors.
   *
   * @param args command-line arguments
   * @return parsed CommandLineOptions
   */
  private static CommandLineOptions parseCommandLineOptions(String... args) {
    try {
      return CommandLineOptions.parse(args);
    } catch (IllegalArgumentException e) {
      log.error("Invalid arguments: {}", e.getMessage());
      System.err.println("Error: " + e.getMessage());
      System.err.println("\nUse --help to see available options");
      System.exit(1);
      throw new AssertionError("System.exit should have terminated JVM");
    }
  }

  /**
   * Gets the latest league ID from the selected options.
   *
   * @param options the command-line options containing year selection
   * @return the league ID for the most recent year selected
   */
  private static String getLatestLeagueId(CommandLineOptions options) {
    PropertiesUtil props = PropertiesUtil.getInstance();

    // Find the latest year from selected years
    int latestYear =
        options.getYears().stream()
            .max(Integer::compareTo)
            .orElse(props.getLatestYear()); // Fallback to configured latest year

    return props.getLeagueIdByYear(latestYear);
  }
}
