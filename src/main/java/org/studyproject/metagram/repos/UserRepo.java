package org.studyproject.metagram.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.studyproject.metagram.domain.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByActivationCode(String code);
}
