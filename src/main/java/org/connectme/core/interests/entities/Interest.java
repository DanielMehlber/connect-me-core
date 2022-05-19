package org.connectme.core.interests.entities;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This entity contains all data of an interest (incl. different terms in different languages).
 * @author Daniel Mehlber
 */
@Table(name="interest_root")
@Entity
public class Interest {

    @Id @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @Column(name = "last_update_on")
    @UpdateTimestamp
    private LocalDateTime updatedOn;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "root")
    private Set<InterestTerm> terms;

    public Interest() {};

    public Interest(InterestTerm... _terms) {
        this();
        terms = new HashSet<>(Arrays.asList(_terms));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<InterestTerm> getTerms() {
        return terms;
    }

    public void setTerms(Set<InterestTerm> terms) {
        this.terms = terms;
    }
}
