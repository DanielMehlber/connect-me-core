package org.connectme.core.interests.impl.jpa;

import org.connectme.core.interests.entities.InterestTerm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestTermRepository extends CrudRepository<InterestTerm, Long> {
    public List<InterestTerm> searchByTerm(final String term);
}
