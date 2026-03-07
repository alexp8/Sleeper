package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for loading and accessing application properties. Loads properties from
 * application.properties file in the classpath. Dynamically discovers all league years from
 * properties matching the pattern "sleeper.league.id.YYYY".
 */
@Slf4j
@Getter
public class PropertiesUtil {
  private static final String PROPERTIES_FILE = "application.properties";
  private static final String LEAGUE_ID_PREFIX = "sleeper.league.id.";
  private static final PropertiesUtil INSTANCE = new PropertiesUtil();

  private final String sleeperApiBaseUrl;
  private final Map<Integer, String> leagueIdsByYear;
  private final List<Integer> availableYears;
  private final List<String> allLeagueIds;

  /**
   * Private constructor that loads and initializes all properties. This is called once when the
   * class is first loaded via the static INSTANCE field. Dynamically discovers all league years
   * from properties.
   */
  private PropertiesUtil() {
    Properties properties = loadProperties();

    this.sleeperApiBaseUrl = getRequiredProperty(properties, "sleeper.api.base.url");
    this.leagueIdsByYear = new TreeMap<>(); // TreeMap for sorted years

    // Dynamically discover all league IDs from properties
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(LEAGUE_ID_PREFIX)) {
        String yearString = key.substring(LEAGUE_ID_PREFIX.length());
        try {
          int year = Integer.parseInt(yearString);
          String leagueId = getRequiredProperty(properties, key);
          leagueIdsByYear.put(year, leagueId);
          log.debug("Discovered league ID for year {}: {}", year, leagueId);
        } catch (NumberFormatException e) {
          log.warn("Invalid year format in property key: {}", key);
        }
      }
    }

    if (leagueIdsByYear.isEmpty()) {
      throw new RuntimeException("No league IDs found in properties file");
    }

    this.availableYears = List.copyOf(leagueIdsByYear.keySet());
    this.allLeagueIds = List.copyOf(leagueIdsByYear.values());

    log.info(
        "Successfully initialized PropertiesUtil with {} league years: {}",
        availableYears.size(),
        availableYears);
  }

  /**
   * Gets the singleton instance of PropertiesUtil.
   *
   * @return the PropertiesUtil instance
   */
  public static PropertiesUtil getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the league ID for a specific year.
   *
   * @param year the year to get the league ID for
   * @return the league ID for the specified year
   * @throws IllegalArgumentException if the year is not configured
   */
  public String getLeagueIdByYear(int year) {
    String leagueId = leagueIdsByYear.get(year);
    if (leagueId == null) {
      throw new IllegalArgumentException(
          "No league ID configured for year " + year + ". Available years: " + availableYears);
    }
    return leagueId;
  }

  /**
   * Gets league IDs for the specified years.
   *
   * @param years the years to get league IDs for
   * @return list of league IDs for the specified years
   * @throws IllegalArgumentException if any year is not configured
   */
  public List<String> getLeagueIdsByYears(Collection<Integer> years) {
    return years.stream().map(this::getLeagueIdByYear).collect(Collectors.toList());
  }

  /**
   * Gets the most recent (latest) year configured.
   *
   * @return the latest year
   */
  public int getLatestYear() {
    return availableYears.get(availableYears.size() - 1);
  }

  /**
   * Gets the league ID for the most recent year.
   *
   * @return the league ID for the latest year
   */
  public String getLatestLeagueId() {
    return getLeagueIdByYear(getLatestYear());
  }

  /**
   * Checks if a specific year is configured.
   *
   * @param year the year to check
   * @return true if the year is configured, false otherwise
   */
  public boolean hasYear(int year) {
    return leagueIdsByYear.containsKey(year);
  }

  /**
   * Loads properties from the application.properties file.
   *
   * @return Properties object containing all loaded properties
   * @throws RuntimeException if properties file cannot be found or loaded
   */
  private Properties loadProperties() {
    Properties properties = new Properties();

    try (InputStream input =
        PropertiesUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {

      if (input == null) {
        log.error("Unable to find {}", PROPERTIES_FILE);
        throw new RuntimeException("Unable to find " + PROPERTIES_FILE);
      }

      properties.load(input);
      log.info("Successfully loaded properties from {}", PROPERTIES_FILE);

    } catch (IOException e) {
      log.error("Error loading properties from {}", PROPERTIES_FILE, e);
      throw new RuntimeException("Error loading properties file", e);
    }

    return properties;
  }

  /**
   * Gets a required property value and validates it's not null or empty.
   *
   * @param properties the Properties object to read from
   * @param key the property key
   * @return the property value
   * @throws RuntimeException if the property is not found or empty
   */
  private String getRequiredProperty(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      log.error("Required property '{}' is missing or empty", key);
      throw new RuntimeException("Required property '" + key + "' is missing or empty");
    }
    return value.trim();
  }
}
