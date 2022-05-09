package org.connectme.core.interests.entities;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This entity contains all data of an interest (incl. different terms in different languages).
 * @author Daniel Mehlber
 */
@Table(name="interest_root")
@Entity
public class Interest {

    @Id @Column(name="id")
    private Long id;

    /**
     * contains different languages and their terms for the interest.
     */
    private Map<String, String> languageTermsMap;

    @OneToMany
    public Set<InterestTerm> interestTerms;

    @Column(name = "CREATED_ON")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @Column(name = "LAST_UPDATE_ON")
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    public Interest() {};

    @OneToMany()
    public void setLanguageTerms(Set<InterestTerm> setLanguageTerms) {
        languageTermsMap = new HashMap<>();
        for(InterestTerm languageTerm : setLanguageTerms) {
            languageTermsMap.put(languageTerm.getLanguageCode(), languageTerm.getTerm());
        }
        this.interestTerms = setLanguageTerms;
    }

    /**
     * Returns term for interest in requested language. If this language is not provided, it will return the english variant.
     * @param language preferred language
     * @return term of interest in language
     * @author Daniel Mehlber
     */
    public String getTermInLanguage(final String language) {
        return languageTermsMap.getOrDefault(language, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<InterestTerm> getInterestTerms() {
        return interestTerms;
    }

    public void setInterestTerms(Set<InterestTerm> interestTerms) {
        this.interestTerms = interestTerms;
    }
}
