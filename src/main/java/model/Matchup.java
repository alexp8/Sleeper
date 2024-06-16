package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Matchup {

    private List<String> starters;

    @JsonProperty("roster_id")
    private int rosterId;

    private List<String> players;

    private double points;

    @JsonProperty("players_points")
    private Map<String, Double> playerPoints;

    @JsonProperty("starters_points")
    private List<Double> starterPoints;

    @JsonProperty("matchup_id")
    private int matchupId;

}
