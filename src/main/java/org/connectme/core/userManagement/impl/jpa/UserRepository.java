package org.connectme.core.userManagement.impl.jpa;

import org.connectme.core.userManagement.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
}
