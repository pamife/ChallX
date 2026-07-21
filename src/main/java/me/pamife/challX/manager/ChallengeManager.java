package me.pamife.challX.manager;

import me.pamife.challX.challenge.Challenge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChallengeManager {
    private final List<Challenge> challenges = new ArrayList<>();

    public void registerChallenge(Challenge challenge) {
        challenges.add(challenge);
    }

    public List<Challenge> getChallenges() {
        return Collections.unmodifiableList(challenges);
    }

    @SuppressWarnings("unchecked")
    public <T extends Challenge> T getChallenge(Class<T> clazz) {
        for (Challenge challenge : challenges) {
            if (clazz.isInstance(challenge)) {
                return (T) challenge;
            }
        }
        return null;
    }

    public Challenge getChallengeByName(String name) {
        for (Challenge challenge : challenges) {
            if (challenge.getName().equalsIgnoreCase(name)) {
                return challenge;
            }
        }
        return null;
    }
}
