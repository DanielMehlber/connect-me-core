package org.connectme.core.interests.impl.jpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.connectme.core.global.exceptions.InternalErrorException;
import org.connectme.core.interests.Interests;
import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.exceptions.NoSuchInterestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

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
    public Interest getRootInterestFromTerm(InterestTerm interestTerm) throws NoSuchInterestException, InternalErrorException {
        Long rootId = interestTerm.getRootId();
        Interest root;
        try {
            root = interestRepository.findById(rootId).orElseThrow(() -> new NoSuchInterestException(rootId));
        } catch (final RuntimeException e) {
            // CASE: some internal error occurred
            log.error(String.format("cannot load interest root id:%d from interest term id:%d '%s' due to an internal error: %s",
                    rootId, interestTerm.getId(), interestTerm.getTerm(), e.getMessage()));
            throw new InternalErrorException("cannot load root interest from interest term", e);
        }

        return root;
    }

    @Override
    public String getInterestTermInLanguage(Interest interest, String language) {
        String term = interest.getTermInLanguage(language);
        if(term == null) {
            term = interest.getTermInLanguage("en");
            log.warn(String.format("term for hobby id:%d '%s' is not available in language '%s'",
                    interest.getId(), term, HtmlUtils.htmlEscape(language)));
        }
        return term;
    }
}
