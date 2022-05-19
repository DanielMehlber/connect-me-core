package org.connectme.core.interests.testUtil;

import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.impl.jpa.InterestRepository;
import org.connectme.core.interests.impl.jpa.InterestTermRepository;

import java.util.*;

public class TestInterestData {

    private static List<Interest> interests = new LinkedList<>();


    static {
        // programming
        interests.add(new Interest(
                new InterestTerm("programming", "en"),
                new InterestTerm("programmieren", "de"),
                new InterestTerm("code", "en"),
                new InterestTerm("编程", "ch")
        ));
    }

    public static List<Interest> getInterests() {
        return interests;
    }

    public static Interest getRandomInterest() {
        int index = new Random().nextInt(interests.size());
        return interests.get(index);
    }

    public static InterestTerm getRandomInterestTerm() {
         List<InterestTerm> terms = new ArrayList(getRandomInterest().getTerms());
         int index = new Random().nextInt(terms.size());
         return terms.get(index);
    }

    public static void fillRepository(InterestRepository interestRepository) {
        interestRepository.saveAll(interests);
    }
}
