package org.connectme.core.tests.interests;

import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.impl.jpa.InterestRepository;
import org.connectme.core.tests.interests.testUtil.TestInterestData;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InterestsTests {

    @Autowired
    private InterestRepository interestRepository;

    @BeforeEach
    private void prepare() {
        interestRepository.deleteAll();
    }

    public void searchInterestTerms() throws Exception {
        // -- arrange --
        TestInterestData.fillRepository(interestRepository);
        final InterestTerm interestTerm = TestInterestData.getRandomInterestTerm();

        // -- act --


        // -- assert --

    }

}
