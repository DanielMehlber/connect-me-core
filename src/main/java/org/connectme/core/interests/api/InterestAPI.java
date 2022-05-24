package org.connectme.core.interests.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.connectme.core.global.exceptions.InternalErrorException;
import org.connectme.core.interests.Interests;
import org.connectme.core.interests.entities.InterestTerm;
import org.connectme.core.authentication.beans.UserAuthenticationBean;
import org.connectme.core.userManagement.exceptions.FailedAuthenticationException;
import org.connectme.core.userManagement.exceptions.NoSuchUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@RestController
@RequestMapping("/interests")
public class InterestAPI {

    private final Logger log = LogManager.getLogger(InterestAPI.class);

    @Autowired
    private Interests interests;

    @Autowired
    private UserAuthenticationBean authenticationBean;

    /**
     * Looks up interest terms matching the users search term.
     * This API is called when the user searches for interest(-terms).
     * @param searchTerm search term of user
     * @return list of interest terms matching the users search term
     * @author Daniel Mehlber
     */
    @GetMapping(value = "/search/term", consumes = "text/plain", produces = "application/json")
    public List<InterestTerm> searchTerms(@RequestParam("term") final String searchTerm) {
        log.debug(String.format("requested all terms for search term '%s'", HtmlUtils.htmlEscape(searchTerm)));
        // search for terms in database
        List<InterestTerm> foundTerms = interests.searchInterestTerms(searchTerm);

        log.debug(String.format("search completed: %d terms where found", foundTerms.size()));
        return foundTerms;
    }

}
