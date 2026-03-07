# Sleeper Fantasy Football Analytics

A Java application that fetches and analyzes fantasy football data from the Sleeper API for the Cave Dynasty League across multiple seasons (2022-2024).

## Overview

This project connects to the [Sleeper Fantasy Football API](https://docs.sleeper.com/) to retrieve league data including players, rosters, matchups, and transactions. It then performs various statistical analyses to provide insights into league performance, trading patterns, waiver activity, and player statistics.

## Features

### Data Collection
- Fetches NFL player data
- Retrieves league rosters for multiple seasons
- Collects matchup data for all weeks
- Gathers transaction history (trades and waivers)
- Implements local caching to reduce API calls

### Analytics

#### Trade Analysis (`CalcTrades`)
- Total trades executed per user
- Trades by position per user
- Most traded NFL players
- Most traded players by position (RB, QB, WR, TE)
- FAAB beans spent in trades
- FAAB beans received from trades

#### Waiver Analysis (`CalcWaivers`)
- Total waivers executed per user
- Most dropped NFL players
- Total FAAB beans spent on waivers

#### Matchup Analysis (`CalcMatchups`)
- Total fantasy points scored per user
- Fantasy points by position per user
- Most "donuts" (zero-point performances) by user
- Most "donuts" by NFL player
- Highest weekly fantasy points by player
- Position-specific weekly highs (QB, WR, TE, RB)
- Closest loss analysis
- Losses by less than 10 points
- Points scored from bench players

#### Player Statistics
- Players whose first and last name start with the same letter

## Technology Stack

- **Java 17**
- **Maven** - Build and dependency management
- **Jackson** - JSON processing
- **Lombok** - Reduce boilerplate code
- **SLF4J + Log4j2** - Logging framework
- **Java HttpClient** - HTTP communication

## Project Structure

```
Sleeper/
├── src/main/java/
│   ├── calculation/       # Analytics and calculation logic
│   ├── exception/         # Custom exceptions
│   ├── model/            # Data models (Player, Roster, Matchup, Transaction, User)
│   ├── runner/           # Main entry point
│   ├── service/          # API services (SleeperRest, HttpService)
│   └── util/             # Utility classes (DataHelper, PropertiesUtil, FileHelper)
├── src/main/resources/
│   ├── application.properties  # Configuration
│   └── [cached data files]     # Locally cached API responses
└── pom.xml
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Sleeper API Configuration
sleeper.api.base.url=https://api.sleeper.app/v1

# League IDs
sleeper.league.id.2022=869324695290400768
sleeper.league.id.2023=916422844907630592
sleeper.league.id.2024=1071255073365331968
```

## Setup and Installation

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean compile
```

### Format Code
```bash
mvn fmt:format
```

### Run

#### Basic Usage
Run with default options (all analyses, all years, cached data):
```bash
mvn exec:java -Dexec.mainClass="runner.Main"
```

Or compile and run directly:
```bash
mvn package
java -cp target/classes runner.Main
```

#### Command-Line Options

The application supports the following command-line arguments:

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--refresh` | `-r` | Force refresh data from Sleeper API | `false` (use cached) |
| `--year <years>` | `-y` | Specify years: `2022`, `2023`, `2024`, or `all` | `all` |
| `--analysis <types>` | `-a` | Specify analyses: `trades`, `waivers`, `matchups`, or `all` | `all` |
| `--help` | `-h` | Display help message | - |

**Multiple values:** Separate with commas (e.g., `--year 2023,2024`)

#### Examples

**Refresh all data and run all analyses:**
```bash
java -cp target/classes runner.Main --refresh
```

**Analyze only 2024 trades:**
```bash
java -cp target/classes runner.Main --year 2024 --analysis trades
```

**Refresh and analyze trades and waivers for 2023 and 2024:**
```bash
java -cp target/classes runner.Main -r -y 2023,2024 -a trades,waivers
```

**Using Maven exec plugin with arguments:**
```bash
mvn exec:java -Dexec.mainClass="runner.Main" -Dexec.args="--refresh --year 2024"
```

**Display help:**
```bash
java -cp target/classes runner.Main --help
```

## How It Works

1. **First Run**: Fetches all data from the Sleeper API and caches it locally in `src/main/resources/`
2. **Subsequent Runs**: Uses cached data to avoid unnecessary API calls (set `forceRefresh` parameter to `true` to refresh data)
3. **Analysis**: Processes the data through various calculation modules
4. **Output**: Logs results to console with detailed statistics

## Architecture Highlights

### Separation of Concerns
- **HttpService**: Generic HTTP client for REST API communication
- **SleeperRest**: Sleeper-specific API endpoints and JSON parsing
- **DataHelper**: Data fetching with local caching logic
- **Calculation Classes**: Isolated analytics for different aspects

### Design Patterns
- **Singleton**: PropertiesUtil for configuration management
- **Static Utility Classes**: Stateless operations for data processing
- **Exception Handling**: Custom `SleeperApiException` for API errors

### Code Quality
- Lombok annotations (`@Slf4j`, `@Getter`) for cleaner code
- Comprehensive JavaDoc documentation
- Google Java Format style enforcement via `fmt-maven-plugin`
- Input validation and proper error handling
- SLF4J logging facade with configurable implementation

## API Reference

This project uses the [Sleeper API v1](https://docs.sleeper.com/):

- **GET** `/players/nfl` - All NFL players
- **GET** `/league/{league_id}/rosters` - League rosters
- **GET** `/league/{league_id}/users` - League users
- **GET** `/league/{league_id}/matchups/{week}` - Weekly matchups
- **GET** `/league/{league_id}/transactions/{round}` - League transactions

## Contributing

This is a personal analytics project for the Cave Dynasty League. Feel free to fork and adapt for your own Sleeper leagues.

## License

This project is for personal use and is not affiliated with Sleeper.
