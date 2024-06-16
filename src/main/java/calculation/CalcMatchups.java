package calculation;

import lombok.Builder;
import lombok.Getter;
import model.Matchup;
import model.Player;
import model.Roster;
import model.User;

import java.util.*;
import java.util.stream.Collectors;

public class CalcMatchups extends Calculation {


    public static void calcMatchups(List<Roster> rosters, Map<String, Player> nflPlayers, List<Matchup> matchups, List<User> users) {

        List<String> runningBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.RB);
        List<String> quarterBackIds = Player.getPlayers(nflPlayers.values(), Player.Position.QB);
        List<String> wideReceiverIds = Player.getPlayers(nflPlayers.values(), Player.Position.WR);
        List<String> tightEndIds = Player.getPlayers(nflPlayers.values(), Player.Position.TE);

        List<Metric> metrics = new ArrayList<>();

        // calc cumulative points per user, at every position
        cumulativePointCalculation(rosters, matchups, users, runningBackIds, quarterBackIds, wideReceiverIds, tightEndIds, metrics);

        // peak weekly stats
        weeklyPeakPts(nflPlayers, matchups);


        // close matchups
        closeMatchups(matchups, users, rosters);

    }

    private static void closeMatchups(List<Matchup> matchups, List<User> users, List<Roster> rosters) {

        Matchup loserWithClosestLoss = null;
        double minPointDiff = Double.MAX_VALUE;
        Map<User, Integer> closeLossesPerUser = new HashMap<>();
        for (Matchup matchup : matchups) {

            Matchup otherMatchup = matchups.stream()
                    .filter(m -> m.getMatchupId() == matchup.getMatchupId())
                    .filter(m -> m.getRosterId() != matchup.getRosterId())
                    .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find other matchup for: " + matchup));

            double pointDiff = Math.abs(matchup.getPoints() - otherMatchup.getPoints());
            Matchup loser = matchup.getPoints() < otherMatchup.getPoints() ? matchup : otherMatchup;

            if (pointDiff < minPointDiff) {
                minPointDiff = pointDiff;
                loserWithClosestLoss = loser;
            }

            // if lost matchup by 10 pts, track the user
            if (pointDiff < 10) {

                User user = User.getUserFromMatchup(matchup, rosters, users);

                int losses = closeLossesPerUser.getOrDefault(user, 0);
                closeLossesPerUser.put(user, losses + 1);
            }
        }

        User userLoser = User.getUserFromMatchup(loserWithClosestLoss, rosters, users);
        logger.info("Closest lost by {}pts, {}", minPointDiff, userLoser.getName());

        logger.info("Number of losses by 10 pts or less: {}", closeLossesPerUser.toString());


    }

    private static void cumulativePointCalculation(List<Roster> rosters, List<Matchup> matchups, List<User> users, List<String> runningBackIds, List<String> quarterBackIds, List<String> wideReceiverIds, List<String> tightEndIds, List<Metric> metrics) {

        // for every user, grab how many points they have scored for each position on a starting lineup
        for (User user : users) {

            // find this user's roster
            Roster roster = Roster.getUserRoster(rosters, user.getUserId());

            // find player matchups
            List<Matchup> userMatchups = matchups.stream()
                    .filter(matchup -> matchup.getPoints() > 0)
                    .filter(matchup -> matchup.getRosterId() == roster.getRosterId())
                    .collect(Collectors.toList());

            // starting rb pts
            double rbPoints = userMatchups.stream()
                    .mapToDouble(matchup -> {
                        List<String> startingRbs = matchup.getPlayerPoints().keySet().stream()
                                .filter(playerId -> runningBackIds.contains(playerId))
                                .filter(playerId -> matchup.getStarters().contains(playerId))
                                .collect(Collectors.toList());

                        return startingRbs.stream()
                                .mapToDouble(rb -> matchup.getPlayerPoints().get(rb)).sum();
                    }).sum();
            logger.info("{} rbs got {} points", user.getName(), rbPoints);

            // wr pts
            double wrPoints = userMatchups.stream()
                    .mapToDouble(matchup -> {
                        List<String> wrs = matchup.getPlayerPoints().keySet().stream()
                                .filter(playerId -> wideReceiverIds.contains(playerId))
                                .filter(playerId -> matchup.getStarters().contains(playerId))
                                .collect(Collectors.toList());

                        return wrs.stream()
                                .mapToDouble(wr -> matchup.getPlayerPoints().get(wr)).sum();
                    }).sum();
            logger.info("{} wrs got {} points", user.getName(), wrPoints);

            // te points
            double tePoints = userMatchups.stream()
                    .mapToDouble(matchup -> {
                        List<String> tes = matchup.getPlayerPoints().keySet().stream()
                                .filter(playerId -> tightEndIds.contains(playerId))
                                .filter(playerId -> matchup.getStarters().contains(playerId))
                                .collect(Collectors.toList());

                        return tes.stream()
                                .mapToDouble(te -> matchup.getPlayerPoints().get(te)).sum();
                    })
                    .sum();
            logger.info("{} tes got {} points", user.getName(), tePoints);

            // qb points
            double qbPoints = userMatchups.stream()
                    .mapToDouble(matchup -> {
                        List<String> qbs = matchup.getPlayerPoints().keySet().stream()
                                .filter(playerId -> quarterBackIds.contains(playerId))
                                .filter(playerId -> matchup.getStarters().contains(playerId))
                                .collect(Collectors.toList());

                        return qbs.stream()
                                .mapToDouble(qb -> matchup.getPlayerPoints().get(qb)).sum();
                    })
                    .sum();
            logger.info("{} qbs got {} points", user.getName(), qbPoints);

            double totalPoints = userMatchups.stream()
                    .mapToDouble(Matchup::getPoints).sum();
            logger.info("{} has scored a total of {} points", user.getName(), totalPoints);

            // numDonuts
            int numDonuts = (int) userMatchups.stream()
                    .filter(matchup -> {
                        List<String> starters = matchup.getPlayerPoints().keySet().stream()
                                .filter(playerId -> matchup.getStarters().contains(playerId))
                                .collect(Collectors.toList());

                        return matchup.getPlayerPoints().entrySet().stream()
                                .filter(player -> starters.contains(player.getKey()))
                                .anyMatch(player -> player.getValue() == 0);
                    })
                    .count();
            logger.info("{} got {} donuts", user.getName(), numDonuts);

            Metric metric = Metric.builder()
                    .name(user.getName())
                    .rbPoints(rbPoints)
                    .wrPoints(wrPoints)
                    .tePoints(tePoints)
                    .qbPoints(qbPoints)
                    .totalPoints(totalPoints)
                    .numDonuts(numDonuts)
                    .build();
            metrics.add(metric);

            logger.info("");
        }

        // most total points per position
        Metric mostCumulativeRbPoints = metrics.stream()
                .max(Comparator.comparing(x -> x.rbPoints)).get();
        logger.info("{} got the most RB points {}", mostCumulativeRbPoints.name, mostCumulativeRbPoints.rbPoints);

        Metric mostCumulativeWrPoints = metrics.stream()
                .max(Comparator.comparing(x -> x.wrPoints)).get();
        logger.info("{} got the most WR points {}", mostCumulativeWrPoints.name, mostCumulativeWrPoints.wrPoints);

        Metric mostCumulativeTePoints = metrics.stream()
                .max(Comparator.comparing(x -> x.tePoints)).get();
        logger.info("{} got the most TE points {}", mostCumulativeTePoints.name, mostCumulativeTePoints.tePoints);

        Metric mostCumulativeQbPoints = metrics.stream()
                .max(Comparator.comparing(x -> x.qbPoints)).get();
        logger.info("{} got the most QB points {}", mostCumulativeQbPoints.name, mostCumulativeQbPoints.qbPoints);

        Metric mostCumulativeUserDonuts = metrics.stream()
                .max(Comparator.comparing(x -> x.numDonuts)).get();
        logger.info("{} got the most donuts {}", mostCumulativeUserDonuts.name, mostCumulativeUserDonuts.numDonuts);
    }

    private static void weeklyPeakPts(Map<String, Player> nflPlayers, List<Matchup> matchups) {

        List<Starter> starters = new ArrayList<>();
        for (Player player : nflPlayers.values()) {

            int numDonuts = (int) matchups.stream()
                    .filter(matchup -> matchup.getPoints() > 0)
                    .filter(matchup -> matchup.getStarters().contains(player.getPlayerId())) // filter on this player
                    .filter(matchup -> matchup.getPlayerPoints().containsKey(player.getPlayerId()) && matchup.getPlayerPoints().get(player.getPlayerId()) == 0) // find how many donuts
                    .count();

            double mostPoints = matchups.stream()
                    .filter(matchup -> matchup.getPoints() > 0)
                    .filter(matchup -> matchup.getStarters().contains(player.getPlayerId())) // filter on this player
                    .filter(matchup -> matchup.getPlayerPoints().containsKey(player.getPlayerId()))
                    .mapToDouble(matchup -> matchup.getPlayerPoints().get(player.getPlayerId())).max().orElse(0d);

            Starter starter = Starter.builder()
                    .name(nflPlayers.get(player.getPlayerId()).getName())
                    .numDonuts(numDonuts)
                    .playerId(player.getPlayerId())
                    .maxMatchupPts(mostPoints)
                    .build();
            starters.add(starter);
        }
        List<Starter> mostPlayerDonuts = starters.stream()
                .sorted(Comparator.comparing(Starter::getNumDonuts).reversed())
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Most donuts: {}", mostPlayerDonuts.stream().map(x -> x.name + ": " + x.numDonuts).collect(Collectors.joining(",")));

        // highest peak pts
        List<Starter> peakMatchupPts = starters.stream()
                .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Highest weekly pts {}", peakMatchupPts.stream().map(x -> x.name + ", pts=" + x.maxMatchupPts).collect(Collectors.joining(",")));

        // highest peak qb pts
        List<Starter> peakQbPts = starters.stream()
                .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
                .filter(starter -> nflPlayers.get(starter.getPlayerId()).getPosition() == Player.Position.QB)
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Most QB pts: {}", peakQbPts.stream().map(x -> x.name + ", pts=" + x.maxMatchupPts).collect(Collectors.joining(",")));

        // highest peak wr pts
        List<Starter> peakWrPts = starters.stream()
                .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
                .filter(starter -> nflPlayers.get(starter.getPlayerId()).getPosition() == Player.Position.WR)
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Most WR pts: {}", peakWrPts.stream().map(x -> x.name + ", pts=" + x.maxMatchupPts).collect(Collectors.joining(",")));

        // highest peak te pts
        List<Starter> peakTePts = starters.stream()
                .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
                .filter(starter -> nflPlayers.get(starter.getPlayerId()).getPosition() == Player.Position.TE)
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Most TE pts: {}", peakTePts.stream().map(x -> x.name + ", pts=" + x.maxMatchupPts).collect(Collectors.joining(",")));

        // highest peak rb pts
        List<Starter> peakRbPts = starters.stream()
                .sorted(Comparator.comparing(Starter::getMaxMatchupPts).reversed())
                .filter(starter -> nflPlayers.get(starter.getPlayerId()).getPosition() == Player.Position.RB)
                .limit(10)
                .collect(Collectors.toList());
        logger.info("Most RB pts: {}", peakRbPts.stream().map(x -> x.name + ", pts=" + x.maxMatchupPts).collect(Collectors.joining(",")));
    }

    @Builder
    @Getter
    public static class Starter {
        private final String name;
        private final String playerId;
        private final int numDonuts;
        private final double maxMatchupPts;

        @Override
        public String toString() {
            return "name='" + name + '\'' + ", numDonuts=" + numDonuts;
        }
    }

    @Builder
    public static class Metric {
        private final String name;
        private final double rbPoints;
        private final double tePoints;
        private final double qbPoints;
        private final double wrPoints;
        private final double totalPoints;
        private final int numDonuts;
    }
}
