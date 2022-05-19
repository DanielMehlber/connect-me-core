package org.connectme.core.interests;

import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.impl.jpa.InterestRepository;
import org.connectme.core.interests.testUtil.TestInterestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class InterestsTests {

    @Autowired
    private Interests interestManagement;

    @Autowired
    private InterestRepository interestRepository;

    @BeforeEach
    private void prepare() {
        interestRepository.deleteAll();
    }

    @Test
    public void searchInterestTerms() throws Exception {
        // -- arrange --
        TestInterestData.fillRepository(interestRepository);
        final InterestTerm interestTerm = TestInterestData.getRandomInterestTerm();
        final String term = interestTerm.getTerm();

        // -- act --
        List<InterestTerm> terms = interestManagement.searchInterestTerms(term);

        // -- assert --
        for(InterestTerm fetchedTerm : terms) {
            String strTern = fetchedTerm.getTerm();
            Assertions.assertTrue(strTern.contains(term));
        }
    }

}
