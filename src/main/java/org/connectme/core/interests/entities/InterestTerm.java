package org.connectme.core.interests.entities;

import javax.persistence.*;

@Entity
@Table(name="interest_language_term")
public class InterestTerm {

    @Id @Column(name="id")
    private Long id;

    @Column(name="interest_term")
    private Long rootId;

    @Column(name="lang")
    private String languageCode;

    @Column(name="term")
    private String term;

    public InterestTerm() {}

    public InterestTerm(String term, String lang) {
        this.term = term;
        this.languageCode = lang;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
