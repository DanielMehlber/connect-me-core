package org.connectme.core.interests.entities;

import javax.persistence.*;

@Entity
@Table(name="interest_term")
public class InterestTerm {

    @Id @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id")
    private Interest root;

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

    public Interest getRoot() {
        return root;
    }

    public void setRoot(Interest root) {
        this.root = root;
    }
}
