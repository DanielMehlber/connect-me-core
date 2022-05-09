package org.connectme.core.interests;

import org.connectme.core.global.exceptions.InternalErrorException;
import org.connectme.core.interests.entities.Interest;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.interests.exceptions.NoSuchInterestException;

import java.util.List;

/**
 * This interface declares all interactions which are required when it comes to managing interests on the platform.
 */
public interface Interests {

    /**
     * Searches for interests in database which are associated or related to the entered term.
     * The search not only goes through the description of all known interests, but also works with various languages.
     * @param term used for searching interests
     * @return list of found interest terms
     */
    List<InterestTerm> searchInterestTerms(final String term);

    /**
     * Determine root interest of interest term.
     * @param interestTerm term for the searched interest
     * @return root interest of passed interestTerm
     * @throws NoSuchInterestException cannot find interest
     * @throws InternalErrorException an unexpected internal error occurred
     */
    Interest getRootInterestFromTerm(final InterestTerm interestTerm) throws NoSuchInterestException, InternalErrorException;

    /**
     * Attempt to find a term for the passed interest in a specific language. If the requested language is not available
     * the english default will be returned.
     * @param interest root interest
     * @param language
     * @return
     */
    String getInterestTermInLanguage(final Interest interest, final String language);


}
