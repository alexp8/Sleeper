package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.NoSuchElementException;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("display_name")
    private String name;

    @Override
    public String toString() {
        return name;
    }

    public static User getUserFromMatchup(@NotNull Matchup matchup, List<Roster> rosters, List<User> users) {
        String userId = rosters.stream()
                .filter(x -> x.getRosterId() == matchup.getRosterId())
                .map(Roster::getOwnerId)
                .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find user id"));

        return users.stream()
                .filter(u -> u.getUserId().equalsIgnoreCase(userId ))
                .findFirst().orElseThrow(() -> new NoSuchElementException(""));
    }
}
