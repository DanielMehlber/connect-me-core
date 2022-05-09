package org.connectme.core.tests.interests.testUtil;

import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.impl.jpa.InterestRepository;

import java.util.*;

public class TestInterestData {

    private static List<Interest> interests;

    static {
        // programming
        Interest programming = new Interest();
        programming.setInterestTerms(new HashSet<InterestTerm>(Arrays.asList(
                new InterestTerm("programmieren", "de"),
                new InterestTerm("coding", "en")
        )));
        interests.add(programming);

        // party
        Interest partying = new Interest();
        partying.setInterestTerms(new HashSet<InterestTerm>(Arrays.asList(
                new InterestTerm("party", "en"),
                new InterestTerm("feiern", "de")
        )));
        interests.add(partying);
    }

    public static List<Interest> getInterests() {
        return interests;
    }

    public static Interest getRandomInterest() {
        int index = new Random().nextInt(interests.size());
        return interests.get(index);
    }

    public static InterestTerm getRandomInterestTerm() {
        Interest randomInterest = getRandomInterest();
        int index = new Random().nextInt(randomInterest.getInterestTerms().size());
        return new ArrayList<InterestTerm>(randomInterest.getInterestTerms()).get(index);
    }

    public static void fillRepository(InterestRepository repository) {
        repository.saveAll(interests);
    }
}
