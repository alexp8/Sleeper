package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
    private Position position;
    private Integer age;

    @JsonProperty("fantasy_positions")
    private List<Position> fantasyPositions;

    @JsonProperty("player_id")
    private String playerId;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    public static List<String> getPlayers(Collection<Player> nflPlayers, Position position) {
        return nflPlayers.stream()
                .filter(x -> Objects.equals(x.getPosition(), position))
                .map(Player::getPlayerId)
                .collect(Collectors.toList());
    }

    public void setPosition(String position) {
        if (position == null) {
            this.position = Position.NONE;
            return;
        }

        Position p;
        try {
            p = Position.valueOf(position);
        } catch (Exception e) {
            p = Position.NONE;
        }
        this.position = p;
    }

    public void setFantasyPositions(List<String> positions) {
        List<Position> fantasyPositions = new ArrayList<>();

        if (positions == null) {
            return;
        }

        for (String p : positions) {
            Position position;
            try {
                position = Position.valueOf(p);
            } catch (Exception e) {
                position = Position.NONE;
            }
            fantasyPositions.add(position);
        }
        this.fantasyPositions = fantasyPositions;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public static enum Position {
        NONE, WR, G, TE, OT, S, RB, LB, DE, FS, T, CB, SS, DB, K, NT, ILB, QB, OL, OLB, LS, DT, FB, C, P, DEF, DL, OG, K_P;
    }

}
