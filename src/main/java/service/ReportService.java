package service;

import calculation.CalcMatchups;
import calculation.CalcTrades;
import calculation.CalcWaivers;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import model.Matchup;
import model.Player;
import model.Roster;
import model.Transaction;
import model.User;
import model.report.MatchupReport;
import model.report.TradeReport;
import model.report.WaiverReport;
import util.ReportWriter;

/**
 * Service responsible for generating and persisting analysis reports.
 *
 * <p>Encapsulates the full report generation pipeline: calculation → persistence → logging.
 * Provides a single point of control for report output, making it easy to extend with additional
 * output destinations (API, database, email, etc.) without modifying calling code.
 */
@Slf4j
public class ReportService {

  // Report output configuration
  private static final String TRADES_DIR = "trades";
  private static final String TRADES_FILE = "trade_report.json";
  private static final String WAIVERS_DIR = "waivers";
  private static final String WAIVERS_FILE = "waiver_report.json";
  private static final String MATCHUPS_DIR = "matchups";
  private static final String MATCHUPS_FILE = "matchup_report.json";

  /**
   * Generate trade analysis report and persist to file system.
   *
   * <p>Generates both JSON and HTML formats for the trade report.
   *
   * @param rosters list of rosters
   * @param nflPlayers map of NFL players
   * @param transactions list of transactions
   * @param users list of users
   */
  public static void generateTradeReport(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Transaction> transactions,
      List<User> users) {

    log.info("Running trade analysis...");
    TradeReport report = CalcTrades.calcTrades(rosters, nflPlayers, transactions, users);

    // Generate JSON report
    persistReport(report, TRADES_DIR, TRADES_FILE);

    // Generate HTML report
    util.TemplateRenderer.renderReport(report, "trades.html", TRADES_DIR, "trade_report.html");

    log.info("Trade reports (JSON and HTML) written to {}", TRADES_DIR);
  }

  /**
   * Generate waiver analysis report and persist to file system.
   *
   * @param rosters list of rosters
   * @param nflPlayers map of NFL players
   * @param transactions list of transactions
   * @param users list of users
   */
  public static void generateWaiverReport(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Transaction> transactions,
      List<User> users) {

    log.info("Running waiver analysis...");
    WaiverReport report = CalcWaivers.calcWaivers(rosters, nflPlayers, transactions, users);
    persistReport(report, WAIVERS_DIR, WAIVERS_FILE);
    log.info("Waiver report written to {}/{}", WAIVERS_DIR, WAIVERS_FILE);
  }

  /**
   * Generate matchup analysis report and persist to file system.
   *
   * <p>Also performs additional analysis on players with matching first/last initials.
   *
   * @param rosters list of rosters
   * @param nflPlayers map of NFL players
   * @param matchups list of matchups
   * @param users list of users
   */
  public static void generateMatchupReport(
      List<Roster> rosters,
      Map<String, Player> nflPlayers,
      List<Matchup> matchups,
      List<User> users) {

    log.info("Running matchup analysis...");
    MatchupReport report = CalcMatchups.calcMatchups(rosters, nflPlayers, matchups, users);
    persistReport(report, MATCHUPS_DIR, MATCHUPS_FILE);
    log.info("Matchup report written to {}/{}", MATCHUPS_DIR, MATCHUPS_FILE);

    // Additional analysis: Find players whose first and last name start with same letter
    logPlayersWithMatchingInitials(nflPlayers, matchups);
  }

  /**
   * Persist a report to the file system using ReportWriter.
   *
   * <p>This method serves as an extension point for adding additional persistence mechanisms (API,
   * database, email, etc.) without modifying calling code.
   *
   * @param report the report object to persist
   * @param directory the subdirectory for the report
   * @param filename the filename for the report
   */
  private static void persistReport(Object report, String directory, String filename) {
    ReportWriter.writeReport(report, directory, filename);
  }

  /**
   * Log players whose first and last names start with the same letter.
   *
   * <p>Only includes players who actually played in matchups. Optimized to O(matchups + players)
   * instead of O(matchups × players).
   *
   * @param nflPlayers map of all NFL players
   * @param matchups list of matchups
   */
  private static void logPlayersWithMatchingInitials(
      Map<String, Player> nflPlayers, List<Matchup> matchups) {

    // Build set of player IDs who played in matchups (O(matchups))
    Set<String> playersInMatchups =
        matchups.stream()
            .flatMap(m -> m.getPlayerPoints().keySet().stream())
            .collect(Collectors.toSet());

    // Find players with matching initials (O(players))
    List<String> playerNames =
        nflPlayers.values().stream()
            .filter(p -> playersInMatchups.contains(p.getPlayerId()))
            .filter(p -> p.getFirstName() != null && !p.getFirstName().isEmpty())
            .filter(p -> p.getLastName() != null && !p.getLastName().isEmpty())
            .filter(p -> p.getFirstName().charAt(0) == p.getLastName().charAt(0))
            .sorted(Comparator.comparing(Player::getName))
            .map(Player::getName)
            .toList();
  }
}
