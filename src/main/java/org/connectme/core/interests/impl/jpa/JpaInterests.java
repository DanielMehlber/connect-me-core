package org.connectme.core.interests.impl.jpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.connectme.core.global.exceptions.InternalErrorException;
import org.connectme.core.interests.Interests;
import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.exceptions.NoInterestTermsFoundException;
import org.connectme.core.interests.exceptions.NoSuchInterestTermException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class JpaInterests implements Interests {

    private final Logger log = LogManager.getLogger(JpaInterests.class);

    @Autowired
    private InterestTermRepository interestTermRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Override
    public List<InterestTerm> searchInterestTerms(String term) {
        return interestTermRepository.searchByTerm(term);
    }

    @Override
    public InterestTerm getInterestTermInLanguage(Interest interest, String language) throws NoInterestTermsFoundException {
        // try to fetch interest terms in language
        List<InterestTerm> interestTerms = interestTermRepository.getByRootIdInLanguage(interest.getId(), language);

        // if there are no interests terms provided for this language fetch the english terms
        if(interestTerms.isEmpty()) {
            log.warn(String.format("term for interest id:%d has been requested for language '%s', but was not provided", interest.getId(), language));
            interestTerms = interestTermRepository.getByRootIdInLanguage(interest.getId(), "en");
        }

        // if there are still no interests, there are none to be displayed
        if(interestTerms.isEmpty()) {
            log.error(String.format("term for interest id:%d is not even provided in english, this should not be", interest.getId()));
            throw new NoInterestTermsFoundException(interest);
        }

        return interestTerms.get(0);
    }

    @Override
    public InterestTerm fetchInterestTerm(Long id) throws NoSuchInterestTermException, InternalErrorException {
        log.debug(String.format("fetching interest term with id:%d from database", id));

        InterestTerm term;
        try {
            term = interestTermRepository.findById(id).orElseThrow(() -> new NoSuchInterestTermException(id));
        } catch (final NoSuchInterestTermException e) {
            log.warn(String.format("cannot fetch interest term with id:%d because it does not exist", id));
            throw e;
        } catch (final RuntimeException e) {
            log.error(String.format("an unexpected runtime error occurred while fetching interest term with id:%d - %s", id, e.getMessage()), e);
            throw new InternalErrorException(String.format("un expected internal error occurred while fetching interest term with id:%d", id), e);
        }

        return term;
    }


}
