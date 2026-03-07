package runner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import util.PropertiesUtil;

/**
 * Parses and validates command-line arguments for the Sleeper analytics application. Supports
 * options for refreshing data, selecting years, and choosing specific analyses.
 */
@Slf4j
@Getter
@AllArgsConstructor
public class CommandLineOptions {

  private final boolean forceRefresh;
  private final Set<Integer> years;
  private final Set<AnalysisType> analyses;
  private final boolean helpRequested;

  /** Enum representing the different types of analyses available. */
  public enum AnalysisType {
    TRADES,
    WAIVERS,
    MATCHUPS,
    ALL
  }

  /**
   * Parses command-line arguments into a CommandLineOptions object.
   *
   * @param args command-line arguments
   * @return parsed CommandLineOptions
   * @throws IllegalArgumentException if invalid arguments are provided
   */
  public static CommandLineOptions parse(String... args) {
    PropertiesUtil props = PropertiesUtil.getInstance();
    boolean forceRefresh = false;
    Set<Integer> years = new HashSet<>(props.getAvailableYears()); // default: all configured years
    Set<AnalysisType> analyses = new HashSet<>(List.of(AnalysisType.ALL)); // default: all
    boolean helpRequested = false;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i].toLowerCase();

      switch (arg) {
        case "--refresh":
        case "-r":
          forceRefresh = true;
          log.debug("Force refresh enabled");
          break;

        case "--help":
        case "-h":
          helpRequested = true;
          break;

        case "--year":
        case "-y":
          if (i + 1 >= args.length) {
            throw new IllegalArgumentException(
                "--year requires a value. Available years: "
                    + props.getAvailableYears()
                    + ", or 'all'");
          }
          years = parseYears(args[++i]);
          break;

        case "--analysis":
        case "-a":
          if (i + 1 >= args.length) {
            throw new IllegalArgumentException(
                "--analysis requires a value (trades, waivers, matchups, or all)");
          }
          analyses = parseAnalyses(args[++i]);
          break;

        default:
          throw new IllegalArgumentException("Unknown argument: " + args[i]);
      }
    }

    // If ALL is selected, include all specific analyses
    if (analyses.contains(AnalysisType.ALL)) {
      analyses = new HashSet<>(List.of(AnalysisType.ALL));
    }

    return new CommandLineOptions(forceRefresh, years, analyses, helpRequested);
  }

  /**
   * Parses year argument value.
   *
   * @param value the year value (e.g., 2022, 2023, 2024, or all)
   * @return set of years to analyze
   * @throws IllegalArgumentException if invalid year value
   */
  private static Set<Integer> parseYears(String value) {
    PropertiesUtil props = PropertiesUtil.getInstance();
    List<Integer> availableYears = props.getAvailableYears();
    Set<Integer> years = new HashSet<>();
    String[] parts = value.split(",");

    for (String part : parts) {
      part = part.trim().toLowerCase();
      if (part.equals("all")) {
        return new HashSet<>(availableYears);
      }

      try {
        int year = Integer.parseInt(part);
        if (!props.hasYear(year)) {
          throw new IllegalArgumentException(
              "Invalid year: " + year + ". Available years: " + availableYears + ", or 'all'");
        }
        years.add(year);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Invalid year format: '"
                + part
                + "'. Available years: "
                + availableYears
                + ", or 'all'");
      }
    }

    log.debug("Years selected: {}", years);
    return years;
  }

  /**
   * Parses analysis argument value.
   *
   * @param value the analysis value (trades, waivers, matchups, or all)
   * @return set of analyses to run
   * @throws IllegalArgumentException if invalid analysis value
   */
  private static Set<AnalysisType> parseAnalyses(String value) {
    Set<AnalysisType> analyses = new HashSet<>();
    String[] parts = value.split(",");

    for (String part : parts) {
      part = part.trim().toLowerCase();
      switch (part) {
        case "trades":
          analyses.add(AnalysisType.TRADES);
          break;
        case "waivers":
          analyses.add(AnalysisType.WAIVERS);
          break;
        case "matchups":
          analyses.add(AnalysisType.MATCHUPS);
          break;
        case "all":
          return new HashSet<>(List.of(AnalysisType.ALL));
        default:
          throw new IllegalArgumentException(
              "Invalid analysis type: "
                  + part
                  + ". Valid types are: trades, waivers, matchups, or all");
      }
    }

    log.debug("Analyses selected: {}", analyses);
    return analyses;
  }

  /**
   * Checks if a specific analysis type should be run based on the selected analyses.
   *
   * @param type the analysis type to check
   * @return true if the analysis should be run
   */
  public boolean shouldRunAnalysis(AnalysisType type) {
    return analyses.contains(AnalysisType.ALL) || analyses.contains(type);
  }

  /**
   * Checks if a specific year should be included based on the selected years.
   *
   * @param year the year to check
   * @return true if the year should be included
   */
  public boolean shouldIncludeYear(int year) {
    return years.contains(year);
  }

  /**
   * Gets all selected league IDs based on the year selection.
   *
   * @return list of league IDs for selected years
   */
  public List<String> getSelectedLeagueIds() {
    PropertiesUtil props = PropertiesUtil.getInstance();
    return props.getLeagueIdsByYears(years);
  }

  /**
   * Displays usage/help information.
   *
   * @return help message as string
   */
  public static String getHelpMessage() {
    PropertiesUtil props = PropertiesUtil.getInstance();
    List<Integer> availableYears = props.getAvailableYears();
    String yearsString =
        availableYears.stream()
            .map(String::valueOf)
            .collect(java.util.stream.Collectors.joining(", "));

    // Get example years for documentation (last two years if available)
    String exampleYears =
        availableYears.size() >= 2
            ? availableYears.get(availableYears.size() - 2)
                + ","
                + availableYears.get(availableYears.size() - 1)
            : availableYears.get(0).toString();
    int latestYear = props.getLatestYear();

    return String.format(
        """
        Sleeper Fantasy Football Analytics

        Usage: java -jar sleeper.jar [OPTIONS]

        Options:
          -r, --refresh              Force refresh data from Sleeper API (default: use cached data)
          -y, --year <years>         Specify years to analyze: %s, or all (default: all)
                                     Multiple years: --year %s
          -a, --analysis <types>     Specify analyses to run: trades, waivers, matchups, or all (default: all)
                                     Multiple types: --analysis trades,waivers
          -h, --help                 Display this help message

        Examples:
          java -jar sleeper.jar
                                     Run all analyses for all years with cached data

          java -jar sleeper.jar --refresh
                                     Refresh data and run all analyses

          java -jar sleeper.jar --year %d --analysis trades
                                     Analyze %d trades only using cached data

          java -jar sleeper.jar -r -y %s -a trades,waivers
                                     Refresh data and analyze trades and waivers for %s

          java -jar sleeper.jar --help
                                     Display this help message
        """,
        yearsString, exampleYears, latestYear, latestYear, exampleYears, exampleYears);
  }

  @Override
  public String toString() {
    return String.format(
        "CommandLineOptions{forceRefresh=%s, years=%s, analyses=%s}",
        forceRefresh, years, analyses);
  }
}
